import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author moli
 * @time 2024-07-15 11:15:32
 */
@SpringBootTest
public class InitData {

    @Resource
    private DataSource dataSource;

    @Test
    public void init(){
        try {
            System.out.println("开始建表。。。");
            //创建引擎配置对象
            ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
            //创建流程引擎对象
            //目标生成数据库表
            configuration.setDataSource(dataSource);

            //设置表的生成策略
            /**
             public static final String DB_SCHEMA_UPDATE_FALSE = "false";不能自动创建表，需要表存在
             public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";先删除表再创建表
             public static final String DB_SCHEMA_UPDATE_TRUE = "true";如果表不存在，自动创建表
             */
            configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
            ProcessEngine processEngine = configuration.buildProcessEngine();
            System.out.println(processEngine.getName() + "***********");
            System.out.println("建表结束。。。");
        }catch (Exception e){
            System.out.println("建表失败！！！");
            e.printStackTrace();
        }
    }

}
