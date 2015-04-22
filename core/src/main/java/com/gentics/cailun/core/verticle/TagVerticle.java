package com.gentics.cailun.core.verticle;

import static com.gentics.cailun.util.JsonUtils.fromJson;
import static com.gentics.cailun.util.JsonUtils.toJson;
import static io.vertx.core.http.HttpMethod.DELETE;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static io.vertx.core.http.HttpMethod.PUT;
import io.vertx.core.AsyncResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Route;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jacpfx.vertx.spring.SpringVerticle;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;

import com.gentics.cailun.core.AbstractProjectRestVerticle;
import com.gentics.cailun.core.data.model.Content;
import com.gentics.cailun.core.data.model.I18NProperties;
import com.gentics.cailun.core.data.model.Language;
import com.gentics.cailun.core.data.model.Project;
import com.gentics.cailun.core.data.model.Tag;
import com.gentics.cailun.core.data.model.auth.PermissionType;
import com.gentics.cailun.core.data.model.auth.User;
import com.gentics.cailun.core.data.model.relationship.Translated;
import com.gentics.cailun.core.data.service.LanguageService;
import com.gentics.cailun.core.data.service.TagService;
import com.gentics.cailun.core.rest.common.response.GenericMessageResponse;
import com.gentics.cailun.core.rest.content.response.ContentListResponse;
import com.gentics.cailun.core.rest.tag.request.TagCreateRequest;
import com.gentics.cailun.core.rest.tag.request.TagUpdateRequest;
import com.gentics.cailun.core.rest.tag.response.TagListResponse;
import com.gentics.cailun.paging.PagingInfo;
import com.gentics.cailun.util.RestModelPagingHelper;

/**
 * The tag verticle provides rest endpoints which allow manipulation and handling of tag related objects.
 * 
 * @author johannes2
 *
 */
@Component
@Scope("singleton")
@SpringVerticle
public class TagVerticle extends AbstractProjectRestVerticle {

	private static final Logger log = LoggerFactory.getLogger(TagVerticle.class);

	@Autowired
	private TagService tagService;

	@Autowired
	private Neo4jTemplate template;

	@Autowired
	LanguageService languageService;

	public TagVerticle() {
		super("tags");
	}

	@Override
	public void registerEndPoints() throws Exception {
		route("/*").handler(springConfiguration.authHandler());
		addCreateHandler();
		addReadHandler();
		addUpdateHandler();
		addDeleteHandler();

		addTagSubTagHandlers();
		addTagContentHandlers();
	}

	// TODO fetch project specific tag
	// TODO update other fields as well?
	// TODO Update user information
	// TODO use schema and only handle those i18n properties that were specified within the schema.
	private void addUpdateHandler() {
		Route route = route("/:uuid").method(PUT).consumes(APPLICATION_JSON).produces(APPLICATION_JSON);
		route.handler(rc -> {
			String projectName = getProjectName(rc);
			String uuid = rc.request().params().get("uuid");

			List<String> languageTags = getSelectedLanguageTags(rc);
			loadObjectByUuid(rc, uuid, PermissionType.UPDATE, (AsyncResult<Tag> rh) -> {
				try (Transaction tx = graphDb.beginTx()) {

					Tag tag = rh.result();

					TagUpdateRequest requestModel = fromJson(rc, TagUpdateRequest.class);
					// Iterate through all properties and update the changed ones
					for (String languageTag : languageTags) {
						Language language = languageService.findByLanguageTag(languageTag);
						if (language != null) {
							Map<String, String> properties = requestModel.getProperties(languageTag);
							if (properties != null) {
								I18NProperties i18nProperties = tagService.getI18NProperties(tag, language);
								for (Map.Entry<String, String> set : properties.entrySet()) {
									String key = set.getKey();
									String value = set.getValue();
									String i18nValue = i18nProperties.getProperty(key);
									if (i18nValue == null) {
										i18nProperties.setProperty(key, value);
									} else {
										if (!value.equals(i18nValue)) {
											i18nProperties.setProperty(key, value);
										}
									}
								}

								/*
								 * Check whether there are any key missing in the request. This would mean we should remove those i18n properties. First lets
								 * collect thosekeys
								 */
								Set<String> keysToBeRemoved = new HashSet<>();
								for (String i18nKey : i18nProperties.getProperties().getPropertyKeys()) {
									if (!properties.containsKey(i18nKey)) {
										keysToBeRemoved.add(i18nKey);
									}
								}

								/* Now remove the keys */
								for (String key : keysToBeRemoved) {
									i18nProperties.removeProperty(key);
								}

							}
						}
					}
					tag = tagService.save(tag);
					rc.response().setStatusCode(200).end(toJson(tagService.transformToRest(rc, tag, languageTags, 0)));
					tx.success();
				}
			});

		});

	}

	private void addCreateHandler() {
		Route route = route("/").method(POST).consumes(APPLICATION_JSON).produces(APPLICATION_JSON);
		route.handler(rc -> {
			String projectName = getProjectName(rc);
			List<String> languageTags = getSelectedLanguageTags(rc);
			try (Transaction tx = graphDb.beginTx()) {

				TagCreateRequest request = fromJson(rc, TagCreateRequest.class);
				// TODO load project specific root tag
				loadObjectByUuid(rc, request.getTagUuid(), PermissionType.CREATE, (AsyncResult<Tag> rh) -> {
					Tag rootTag = rh.result();

					Tag newTag = new Tag();
					// TODO load schema and set the reference to the tag
					// newTag.setSchemaName(request.getSchemaName());
					// TODO maybe projects should not be a set?
						Project project = projectService.findByName(projectName);
						newTag.addProject(project);
						// TODO handle creator

						// Add the i18n properties to the newly created tag
						for (String languageTag : request.getProperties().keySet()) {
							Map<String, String> i18nProperties = request.getProperties(languageTag);
							Language language = languageService.findByLanguageTag(languageTag);
							I18NProperties tagProps = new I18NProperties(language);
							for (Map.Entry<String, String> entry : i18nProperties.entrySet()) {
								tagProps.setProperty(entry.getKey(), entry.getValue());
							}
							// Create the relationship to the i18n properties
							Translated translated = new Translated(newTag, tagProps, language);
							newTag.getI18nTranslations().add(translated);
						}
						newTag = tagService.save(newTag);
						rc.response().setStatusCode(200).end(toJson(tagService.transformToRest(rc, newTag, languageTags, 0)));
						tx.success();
					});
			}

		});
	}

	// TODO filtering, sorting
	private void addReadHandler() {

		Route route = route("/:uuid").method(GET);
		route.handler(rc -> {
			List<String> languages = getSelectedLanguageTags(rc);
			int depth = getDepth(rc);
			loadObject(rc, "uuid", PermissionType.READ, (AsyncResult<Tag> rh) -> {
				Tag tag = rh.result();
				rc.response().setStatusCode(200).end(toJson(tagService.transformToRest(rc, tag, languages, depth)));
			});
		});

		Route readAllRoute = route("/").method(GET);
		readAllRoute.handler(rc -> {
			String projectName = getProjectName(rc);

			TagListResponse listResponse = new TagListResponse();
			List<String> languageTags = getSelectedLanguageTags(rc);
			int depth = getDepth(rc);

			vertx.executeBlocking(bch -> {
				PagingInfo pagingInfo = getPagingInfo(rc);
				Page<Tag> tagPage = tagService.findAllVisible(rc, projectName, languageTags, pagingInfo);
				for (Tag tag : tagPage) {
					listResponse.getData().add(tagService.transformToRest(rc, tag, languageTags, depth));
				}
				RestModelPagingHelper.setPaging(listResponse, tagPage, pagingInfo);
			}, rh -> {
				rc.response().setStatusCode(200).end(toJson(listResponse));
			});

		});

	}

	// TODO filter by projectName
	private void addDeleteHandler() {
		Route route = route("/:uuid").method(DELETE).produces(APPLICATION_JSON);
		route.handler(rc -> {
			String projectName = getProjectName(rc);
			String uuid = rc.request().params().get("uuid");
			loadObject(rc, "uuid", PermissionType.DELETE, (AsyncResult<Tag> rh) -> {
				Tag tag = rh.result();
				tagService.delete(tag);
				rc.response().setStatusCode(200).end(toJson(new GenericMessageResponse(i18n.get(rc, "tag_deleted", uuid))));
			});
		});
	}

	private void addTagContentHandlers() {
		Route getRoute = route("/:uuid/contents/").method(GET).produces(APPLICATION_JSON);
		getRoute.handler(rc -> {
			try (Transaction tx = graphDb.beginTx()) {
				String projectName = getProjectName(rc);
				ContentListResponse listResponse = new ContentListResponse();
				List<String> languageTags = getSelectedLanguageTags(rc);
				int depth = getDepth(rc);

				loadObject(rc, "uuid", PermissionType.READ, (AsyncResult<Tag> rh) -> {
					Tag rootTag = rh.result();

					PagingInfo pagingInfo = getPagingInfo(rc);
					// User requestUser = springConfiguration.authService().getUser(rc);
						User requestUser = null;
						// TODO filtering, sorting
						Page<Content> contentPage = tagService.findAllVisibleSubContents(rc, projectName, rootTag, languageTags, pagingInfo);
						for (Content content : contentPage) {
							listResponse.getData().add(contentService.transformToRest(rc, content, languageTags, depth));
						}
						RestModelPagingHelper.setPaging(listResponse, contentPage, pagingInfo);
						rc.response().setStatusCode(200).end(toJson(listResponse));
					});
			}
		});
	}

	// TODO filtering, sorting
	private void addTagSubTagHandlers() {
		Route getRoute = route("/:uuid/tags/").method(GET).produces(APPLICATION_JSON);
		getRoute.handler(rc -> {

			try (Transaction tx = graphDb.beginTx()) {
				String projectName = getProjectName(rc);
				TagListResponse listResponse = new TagListResponse();
				List<String> languageTags = getSelectedLanguageTags(rc);
				int depth = getDepth(rc);

				loadObject(rc, "uuid", PermissionType.READ, (AsyncResult<Tag> rh) -> {
					Tag rootTag = rh.result();

					PagingInfo pagingInfo = getPagingInfo(rc);

					Page<Tag> tagPage = tagService.findAllVisibleSubTags(rc, projectName, rootTag, languageTags, pagingInfo);
					for (Tag tag : tagPage) {
						listResponse.getData().add(tagService.transformToRest(rc, tag, languageTags, depth));
					}
					RestModelPagingHelper.setPaging(listResponse, tagPage, pagingInfo);
					rc.response().setStatusCode(200).end(toJson(listResponse));
				});
			}
		});

		Route postRoute = route("/:tagUuid/tags/:subtagUuid").method(POST).produces(APPLICATION_JSON);
		postRoute.handler(rc -> {
			// String tagUuid = rc.request().params().get("tagUuid");
			// String subTagUuid = rc.request().params().get("subtagUuid");
			// TODO be more specific
			// if (StringUtils.isEmpty(tagUuid) || StringUtils.isEmpty(subTagUuid)) {
			// throw new HttpStatusCodeErrorException(404, "Missing uuid parameter");
			// }
				String projectName = getProjectName(rc);
				List<String> languageTags = getSelectedLanguageTags(rc);
				try (Transaction tx = graphDb.beginTx()) {

					loadObject(rc, "tagUuid", PermissionType.UPDATE, (AsyncResult<Tag> rh) -> {
						loadObject(rc, "subtagUuid", PermissionType.READ, (AsyncResult<Tag> srh) -> {
							Tag tag = rh.result();
							Tag subTag = srh.result();

							tag.addTag(subTag);
							tag = tagService.save(tag);

							rc.response().setStatusCode(200).end(toJson(tagService.transformToRest(rc, tag, languageTags, 0)));
							tx.success();
						});

					});

				}
			});

		//TODO fix error handling. This does not fail when tagUuid could not be found
		Route deleteRoute = route("/:tagUuid/tags/:subtagUuid").method(DELETE).produces(APPLICATION_JSON);
		deleteRoute.handler(rc -> {
			// String tagUuid = rc.request().params().get("tagUuid");
			// String subTagUuid = rc.request().params().get("subtagUuid");
			// TODO be more specific?
			// if (StringUtils.isEmpty(tagUuid) || StringUtils.isEmpty(subTagUuid)) {
			// throw new HttpStatusCodeErrorException(404, "Missing uuid parameter");
			// }
				String projectName = getProjectName(rc);
				List<String> languageTags = getSelectedLanguageTags(rc);

				try (Transaction tx = graphDb.beginTx()) {

					loadObject(rc, "tagUuid", PermissionType.UPDATE, (AsyncResult<Tag> rh) -> {
						loadObject(rc, "subtagUuid", PermissionType.READ, (AsyncResult<Tag> srh) -> {
							Tag tag = rh.result();
							Tag subTag = srh.result();
							tag.removeTag(subTag);
							tag = tagService.save(tag);
							rc.response().setStatusCode(200).end(toJson(tagService.transformToRest(rc, tag, languageTags, 0)));
							tx.success();
						});
					});

				}
			});

	}

}
