package com.ccnode.codegenerator.genCode;

import com.ccnode.codegenerator.pojo.DirectoryConfig;
import com.ccnode.codegenerator.pojo.GenCodeRequest;
import com.ccnode.codegenerator.pojo.GenCodeResponse;
import com.ccnode.codegenerator.util.GenCodeConfig;
import com.ccnode.codegenerator.util.IOUtils;
import com.ccnode.codegenerator.util.LoggerWrapper;
import com.ccnode.codegenerator.util.PojoUtil;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * What always stop you is what you always believe.
 * <p>
 * Created by zhengjun.du on 2016/05/21 12:40
 */
public class UserConfigService {

    private final static Logger LOGGER = LoggerWrapper.getLogger(UserConfigService.class);

    public static GenCodeResponse initConfig(GenCodeResponse response){
        LOGGER.info("initConfig");
        try{
            GenCodeConfig config = new GenCodeConfig();
            response.setCodeConfig(config);
            Map<String, String> userConfigMap = response.getUserConfigMap();
            String pojos = userConfigMap.get("pojos");
            if(StringUtils.isBlank(pojos)){
                pojos = Messages.showInputDialog(response.getRequest().getProject(), "Please input Pojo Name : ", "Input Pojos", Messages.getQuestionIcon());
            }
            if(StringUtils.isBlank(pojos)){
                return response.failure("no config or input pojo name");
            }
            pojos = pojos.replace(",","|");
            response.getRequest().setPojoNames(Splitter.on("|").trimResults().omitEmptyStrings().splitToList(pojos));
            for (Map.Entry<String, String> configEntry : response.getUserConfigMap().entrySet()) {
                String key = configEntry.getKey();
                String value = configEntry.getValue();
                check(response.getRequest().getProjectPath(), key,value,config);
                DirectoryConfig directoryConfig = new DirectoryConfig();
                response.setDirectoryConfig(directoryConfig);
                config.setDaoDir(removeStartSplitter(userConfigMap.get("dao.path")));
                config.setSqlDir(removeStartSplitter(userConfigMap.get("sql.path")));
                config.setMapperDir(removeStartSplitter(userConfigMap.get("mapper.path")));
                config.setServiceDir(removeStartSplitter(userConfigMap.get("service.path")));
            }
        }catch(Exception e){
            LOGGER.error(" status error occurred :{}",response,e);
            return response.failure(" status error occurred");
        }

        return response;
    }

    public static String removeStartSplitter(String s){
        if(StringUtils.isBlank(s)){
            return s;
        }
        String splitter = System.getProperty("file.separator");
        if(s.startsWith(splitter)){
            return s.substring(1);
        }
        return s;
    }

    // todo
    private static void check(String projectPath, String key, String value, GenCodeConfig config) {
//        List<String> fileName = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(value);
//        String fileName = IOUtils.matchOnlyOneFile(projectPath, "value");

    }

    public static GenCodeResponse readConfigFile(GenCodeRequest request){
        LOGGER.info("readConfigFile");
        GenCodeResponse ret = new GenCodeResponse();
        ret.accept();
        try{
            String projectPath = request.getProjectPath();
            File propertiesFile = IOUtils.matchOnlyOneFile(projectPath, "generator.properties");
            String fileName = StringUtils.EMPTY;
            if(propertiesFile != null){
                fileName = propertiesFile.getAbsolutePath();
            }
            if(StringUtils.isBlank(fileName)){
                return ret;
//                return ret.failure("error, no generator.properties config file,"
//                        + "please add an generator.properties in you poject path");
            }
            if(Objects.equal(fileName,"NOT_ONLY")){
                LOGGER.error("error, duplicated generator.properties file");
                return ret.failure("","error, duplicated generator.properties file");
            }
            File configFile = new File(fileName);
            List<String> strings = IOUtils.readLines(configFile);
            strings = PojoUtil.avoidEmptyList(strings);
            int lineIndex = 1;
            Map<String,String> configMap = Maps.newHashMap();
            for (String configLine : strings) {
                lineIndex ++;
                if(StringUtils.isBlank(configLine) || configLine.startsWith("#")){
                    continue;
                }
                if(configLine.startsWith("=")){
                    LOGGER.error("line: "+ lineIndex + "error, config key con not be empty");
                    return ret.failure("","line: "+ lineIndex + "error, config key con not be empty");
                }
                List<String> split = Splitter.on("=").trimResults().omitEmptyStrings().splitToList(configLine);
                if(split.size() != 2){

                    LOGGER.error("","line: "+ lineIndex + "config error, correct format : key = value");
                    return ret.failure("","line: "+ lineIndex + "config error, correct format : key = value");
                }
                configMap.put(split.get(0).toLowerCase(),split.get(1));
            }
            ret.setUserConfigMap(configMap);
            LOGGER.info("readConfigFile configMap:{}",configMap);
            return ret;
        }catch(IOException e){
            LOGGER.error(" readConfigFile config file read error ",e);
            return ret.failure(""," readConfigFile config file read error ");
        }catch(Exception e){
            LOGGER.error(" readConfigFile config file read error ",e);
            return ret.failure("","readConfigFile error occurred");
        }
    }

    public static void main(String[] args) {
        String s = "dsfasdjfasdjf";
        System.out.println(s.substring(1));
    }
}
