package cc.feitwnd.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestApiContractTest {

    private static final Set<String> EXPECTED_ENDPOINTS = Set.of(
            "DELETE /admin/article",
            "DELETE /admin/article/comment",
            "DELETE /admin/article/tag",
            "DELETE /admin/articleCategory",
            "DELETE /admin/experience",
            "DELETE /admin/footprint",
            "DELETE /admin/footprint/image",
            "DELETE /admin/friendLink",
            "DELETE /admin/message",
            "DELETE /admin/music",
            "DELETE /admin/operationLog",
            "DELETE /admin/skill",
            "DELETE /admin/socialMedia",
            "DELETE /admin/systemConfig",
            "DELETE /admin/view",
            "DELETE /blog/articleComment/{id}",
            "DELETE /blog/articleLike/{articleId}",
            "DELETE /blog/message/{id}",
            "GET /admin/admin",
            "GET /admin/article/comment/page",
            "GET /admin/article/comment/{articleId}",
            "GET /admin/article/page",
            "GET /admin/article/search",
            "GET /admin/article/tag",
            "GET /admin/article/{id}",
            "GET /admin/articleCategory",
            "GET /admin/experience",
            "GET /admin/footprint",
            "GET /admin/footprint/image",
            "GET /admin/friendLink",
            "GET /admin/message/page",
            "GET /admin/music/page",
            "GET /admin/music/{id}",
            "GET /admin/operationLog/page",
            "GET /admin/personalInfo",
            "GET /admin/report/articleViewTop10",
            "GET /admin/report/overview",
            "GET /admin/report/provinceDistribution",
            "GET /admin/report/viewStatistics",
            "GET /admin/report/visitorStatistics",
            "GET /admin/skill",
            "GET /admin/socialMedia",
            "GET /admin/systemConfig",
            "GET /admin/systemConfig/key/{configKey}",
            "GET /admin/systemConfig/{id}",
            "GET /admin/view/page",
            "GET /admin/visitor/page",
            "GET /blog/article/archive",
            "GET /blog/article/category/{categoryId}",
            "GET /blog/article/detail/{slug}",
            "GET /blog/article/page",
            "GET /blog/article/search",
            "GET /blog/article/tag",
            "GET /blog/article/tag/{tagId}",
            "GET /blog/articleCategory",
            "GET /blog/articleComment/article/{articleId}",
            "GET /blog/articleLike/{articleId}",
            "GET /blog/common/captcha/generate",
            "GET /blog/footprint",
            "GET /blog/footprint/image",
            "GET /blog/friendLink",
            "GET /blog/message",
            "GET /blog/music",
            "GET /blog/personalInfo",
            "GET /blog/report",
            "GET /blog/rss",
            "GET /blog/sitemap.xml",
            "GET /blog/systemConfig/key/{configKey}",
            "GET /cv/experience",
            "GET /cv/personalInfo",
            "GET /cv/skill",
            "GET /health",
            "GET /home/personalInfo",
            "GET /home/socialMedia",
            "GET /home/systemConfig/key/{configKey}",
            "POST /admin/admin/login",
            "POST /admin/admin/logout",
            "POST /admin/admin/sendCode",
            "POST /admin/article",
            "POST /admin/article/comment/reply",
            "POST /admin/article/tag",
            "POST /admin/articleCategory",
            "POST /admin/common/upload",
            "POST /admin/experience",
            "POST /admin/footprint",
            "POST /admin/footprint/image",
            "POST /admin/friendLink",
            "POST /admin/message/reply",
            "POST /admin/music",
            "POST /admin/skill",
            "POST /admin/socialMedia",
            "POST /admin/systemConfig",
            "POST /blog/articleComment",
            "POST /blog/articleLike/{articleId}",
            "POST /blog/message",
            "POST /blog/visitor/record",
            "POST /cv/visitor/record",
            "POST /home/visitor/record",
            "PUT /admin/admin/changeEmail",
            "PUT /admin/admin/changeNickname",
            "PUT /admin/admin/changePassword",
            "PUT /admin/article",
            "PUT /admin/article/comment/approve",
            "PUT /admin/article/publish/{id}",
            "PUT /admin/article/tag",
            "PUT /admin/article/top/{id}",
            "PUT /admin/articleCategory",
            "PUT /admin/experience",
            "PUT /admin/footprint",
            "PUT /admin/footprint/image",
            "PUT /admin/friendLink",
            "PUT /admin/message/approve",
            "PUT /admin/music",
            "PUT /admin/personalInfo",
            "PUT /admin/skill",
            "PUT /admin/socialMedia",
            "PUT /admin/systemConfig",
            "PUT /admin/visitor/block",
            "PUT /admin/visitor/unblock",
            "PUT /blog/articleComment/edit",
            "PUT /blog/message/edit"
    );

    @Test
    void shouldKeepExistingRestEndpoints() throws ClassNotFoundException {
        Set<String> actualEndpoints = discoverEndpoints("cc.feitwnd.controller");

        assertEquals(new TreeSet<>(EXPECTED_ENDPOINTS), actualEndpoints,
                () -> "REST API contract changed. Current endpoints:\n"
                        + String.join("\n", actualEndpoints));
    }

    private Set<String> discoverEndpoints(String basePackage) throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        Set<String> endpoints = new TreeSet<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
            Class<?> controller = Class.forName(candidate.getBeanClassName());
            RequestMapping controllerMapping = AnnotatedElementUtils.findMergedAnnotation(
                    controller, RequestMapping.class);

            for (Method method : controller.getDeclaredMethods()) {
                RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(
                        method, RequestMapping.class);
                if (methodMapping == null) {
                    continue;
                }
                addMappings(endpoints, controllerMapping, methodMapping);
            }
        }
        return endpoints;
    }

    private void addMappings(Set<String> endpoints, RequestMapping controllerMapping,
                             RequestMapping methodMapping) {
        String[] controllerPaths = paths(controllerMapping);
        String[] methodPaths = paths(methodMapping);
        RequestMethod[] requestMethods = methodMapping.method();

        for (String controllerPath : controllerPaths) {
            for (String methodPath : methodPaths) {
                String path = normalizePath(controllerPath, methodPath);
                Arrays.stream(requestMethods)
                        .map(requestMethod -> requestMethod.name() + " " + path)
                        .forEach(endpoints::add);
            }
        }
    }

    private String[] paths(RequestMapping mapping) {
        if (mapping == null) {
            return new String[]{""};
        }
        String[] paths = mapping.path().length > 0 ? mapping.path() : mapping.value();
        return paths.length > 0 ? paths : new String[]{""};
    }

    private String normalizePath(String controllerPath, String methodPath) {
        String path = "/" + controllerPath + "/" + methodPath;
        path = path.replaceAll("/{2,}", "/");
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }
}
