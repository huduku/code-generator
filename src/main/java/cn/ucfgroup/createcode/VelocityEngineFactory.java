package cn.ucfgroup.createcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * VelocityEngine 工厂
 * 
 * @author 毛东亚
 */
public class VelocityEngineFactory {

    public static VelocityEngine generateVelocityEngine(String propertyPath,
            String encode) throws Exception {
        File file = new File(propertyPath);
        return generateVelocityEngine(file, encode);
    }

    public static VelocityEngine generateVelocityEngine(File file, String encode)
            throws Exception {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init(loadProperties(file, encode));
        return velocityEngine;
    }

    public static VelocityEngine generateVelocityEngine(String... templatePaths) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; templatePaths != null && i < templatePaths.length; i++) {
            builder.append(templatePaths[i]);
            if (i < templatePaths.length - 1) {
                builder.append(",");
            }
        }

        Properties properties = new Properties();

        properties.setProperty(RuntimeConstants.RESOURCE_LOADER,
                "file, class, jar");

        properties.setProperty("file.resource.loader.description",
                "Velocity File Resource Loader");
        properties
                .setProperty("file.resource.loader.class",
                        "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        properties.setProperty("file.resource.loader.path", builder.toString());
        properties.setProperty("file.resource.loader.cache", "true");
        properties.setProperty(
                "file.resource.loader.modificationCheckInterval", "30");

        properties.setProperty("class.resource.loader.description",
                "Velocity Classpath Resource Loader");
        properties
                .setProperty("class.resource.loader.class",
                        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        properties.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.apache.velocity.runtime.log.Log4JLogChute");
        properties.setProperty("runtime.log.logsystem.log4j.logger",
                "org.apache.velocity");
        properties.setProperty("directive.set.null.allowed", "true");

        VelocityEngine velocityEngine = new VelocityEngine();
        try {
            velocityEngine.init(properties);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return velocityEngine;
    }

    public static Properties loadProperties(String propertiesPath, String encode)
            throws Exception {
        File file = new File(propertiesPath);
        return loadProperties(file, encode);
    }

    public static Properties loadProperties(File propertiesFile, String encode)
            throws Exception {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new FileInputStream(
                    propertiesFile), encode));
        } catch (Exception e) {
            throw new Exception("加载配置文件错误");
        }
        return properties;
    }
}
