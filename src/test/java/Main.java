import com.vwo.VWO;
import com.vwo.interfaces.integration.IntegrationCallback;
import com.vwo.models.user.VWOContext;
import com.vwo.models.user.GetFlag;
import com.vwo.models.user.VWOInitOptions;
import com.vwo.packages.logger.enums.LogLevelEnum;
import data.StorageTest;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.vwo.models.BatchEventData;
import com.vwo.models.FlushInterface;

public class Main {
    public static void main(String[] args) throws InterruptedException {


        // create flushcallback
        FlushInterface flushCallback = new FlushInterface() {
            @Override
            public void onFlush(String error, String events) {
                System.out.println("Flush callback executed");
                // custom implementation here
            }
        };

        // create an integrations variable which would have an callback function
        IntegrationCallback integrations = new IntegrationCallback() {
            @Override
            public void execute(Map<String, Object> properties) {
                System.out.println("Integration callback executed " + properties);
            }
        };
        Map<String, Object> logger = new HashMap<>();
        logger.put("level", "DEBUG");
        logger.put("prefix", "Shaktiman");

//        logger.put("dateTimeFormat", String.valueOf(System.currentTimeMillis()));
//
//        List<Map<String, Object>> transports = new ArrayList<>();
//        LogTransport logTransport = new LogTransport() {
//            @Override
//            public void log(LogLevelEnum level, String message) {
//                System.out.println("Log level is " + level + " message is " + message);
//            }
//        };
//        transports.add(new HashMap<String, Object>() {{
//            put("level", "INFO");
//            put("defaultTransport", logTransport);
//        }});
//        logger.put("transports", transports);
    


        Map<String, Object> gatewayService = new HashMap<>();
        //gatewayService.put("url", "localhost:8000"); //error
        //gatewayService.put("url", "http://localhost:8000");
        gatewayService.put("url", "http://localhost");
        //gatewayService.put("protocol", "http");
        gatewayService.put("port", "8000");


        //prefix and everything
        VWOInitOptions vwoInitOptions = new VWOInitOptions();
        StorageTest s = new StorageTest();
        vwoInitOptions.setSdkKey("d8aef6abbce89e959947a2ce724bc9ba");
        vwoInitOptions.setAccountId(891469);
        vwoInitOptions.setLogger(logger);

        BatchEventData batchEventData = new BatchEventData();
        batchEventData.setEventsPerRequest(3);  // Set the max number of events per request
        batchEventData.setRequestTimeInterval(10);  // Set the request time interval (in seconds)
        batchEventData.setFlushCallback(flushCallback);
        // Set BatchEventData for batching
        vwoInitOptions.setBatchEventData(batchEventData);

//        vwoInitOptions.setIsUserAliasEnabled(true);
        VWO instance = VWO.init(vwoInitOptions);

        // Loop to test getFlag for 5 different userIds
        for (int i = 1; i <= 5; i++) {
            // Create a new context for each userId
            VWOContext context = new VWOContext();
            String userId = "userId1234" + i; // Create the userId dynamically
            context.setId(userId);

            // Call getFlag with the generated userId
            GetFlag getFlagResponse = instance.getFlag("f1", context);

            // Log the result for each userId
            System.out.println("User ID: " + userId + " - Flag value for 'f1': ");
        }
        instance.flushEvents();
        // VWOContext context = new VWOContext();

        
        // context.setId("saksham000111");
        // GetFlag getFlagResponse = instance.getFlag("f1", context);
        // VWOContext context1 = new VWOContext();
        // context1.setId("dfgh");



//        instance.trackEvent("countPaypalUser",context1);
//        System.out.println(context1.getId());
        int[] ov = new int[3];
        ov[0] = 9;
        ov[1] = 1;
        ov[2] = 3;
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("numberproperty", "first");
        attribute.put("numberproperty", "second");
//        attribute.put("textproperty", null);
//        attribute.put("booleanproperty", false);
        //instance.setAttribute(attribute, context);
//        System.out.println("getFlagResponse -- " + getFlagResponse.isEnabled());
//        System.out.println("getFlagResponse2 -- " + getFlagResponse2.isEnabled());
    }

    public static void appendTextToFile(String fileName, String textToAdd) {
        File file = new File(fileName);
        try {
            // Create the file if it does not exist
            if (!file.exists()) {
                file.createNewFile();
            }


            // Use FileWriter in append mode
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);


            // Write the provided text followed by a newline
            bw.write(textToAdd);
            bw.newLine();


            // Close the BufferedWriter
            bw.close();
            System.out.println("Text added successfully!");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
