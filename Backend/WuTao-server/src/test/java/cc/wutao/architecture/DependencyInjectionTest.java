package cc.wutao.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyInjectionTest {

    @Test
    void productionComponentsShouldUseConstructorInjection() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile("cc\\.wutao\\..*")));

        List<String> fieldInjections = new ArrayList<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents("cc.wutao")) {
            Class<?> type = Class.forName(candidate.getBeanClassName());
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    fieldInjections.add(type.getName() + "#" + field.getName());
                }
            }
        }

        assertTrue(fieldInjections.isEmpty(),
                () -> "Field injection is not allowed:\n" + String.join("\n", fieldInjections));
    }

    @Test
    void internalServicesShouldNotHaveSingleImplementationInterfaces() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory metadataReaderFactory =
                new CachingMetadataReaderFactory(resolver);
        List<String> redundantTypes = new ArrayList<>();

        for (Resource resource : resolver.getResources(
                "classpath*:cc/wutao/service/**/*.class")) {
            String className = metadataReaderFactory.getMetadataReader(resource)
                    .getClassMetadata().getClassName();
            if (className.contains("$")) {
                continue;
            }
            Class<?> type = Class.forName(className);
            if (type.getSimpleName().endsWith("ServiceImpl")
                    || type.isInterface() && type.getSimpleName().endsWith("Service")) {
                redundantTypes.add(className);
            }
        }

        assertTrue(redundantTypes.isEmpty(),
                () -> "Internal services should be concrete classes without Impl suffixes:\n"
                        + String.join("\n", redundantTypes));
    }
}
