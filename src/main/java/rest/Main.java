package rest;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static spark.Spark.*;


public class Main {
    public static void main(String[] args) {
        enableCORS("*", "*", "*");
        put("/stats", new Main()::getFile);
    }

    private Object getFile(Request request, Response response) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info(request.body());
        File file = new File("stats");
        response.raw().setContentType("application/pdf");
        response.raw().setHeader("Content-Disposition", "attachment; filename=" + file.getName() + ".pdf");
        Transcoder transcoder = new PDFTranscoder();
        try {

            OutputStream out = new BufferedOutputStream(response.raw().getOutputStream());
            InputStream in = request.raw().getInputStream();
            TranscoderInput input = new TranscoderInput(in);

            try {
                TranscoderOutput output = new TranscoderOutput(out);
                transcoder.transcode(input, output);

            } finally {
                out.close();
                in.close();
            }
        } catch (Exception e) {
            halt(405, "server error");
        }

        return response.raw();
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }

}
