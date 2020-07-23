package ley.anvil.modpacktools.util;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static ley.anvil.modpacktools.Main.HTTP_CLIENT;

public class FileDownloader {

    private FileDownloader() {
    }

    /**
     * Downloads files asynchronously
     *
     * @param files the files to download
     * @param callback this callback will get the return code of each HTTP request and the file once a file has finished
     * @param existingFileBehaviour what should be done if a file that should be downloaded already exists
     */
    public static void downloadAsync(Map<URL, File> files,
                                     Consumer<AsyncDownloader.DownloadFileTask.Return> callback,
                                     AsyncDownloader.ExistingFileBehaviour existingFileBehaviour) {
        new AsyncDownloader(files,
                callback,
                existingFileBehaviour);
    }

    public static class AsyncDownloader {
        private final Map<URL, File> files;
        private final Consumer<DownloadFileTask.Return> callback;
        private final ExistingFileBehaviour existingFileBehaviour;

        private CountDownLatch latch;

        private AsyncDownloader(Map<URL, File> files,
                                Consumer<DownloadFileTask.Return> callback,
                                ExistingFileBehaviour existingFileBehaviour) {
            this.files = files;
            this.callback = callback;
            this.existingFileBehaviour = existingFileBehaviour;

            this.dispatchTasks();
        }

        private void dispatchTasks() {
            int nFiles = (int)files.entrySet().stream()
                    .filter(f -> existingFileBehaviour == ExistingFileBehaviour.OVERWRITE || !f.getValue().exists())
                    .map(f -> new DownloadFileTask(f.getKey(), f.getValue(), callback))
                    .peek(t -> HTTP_CLIENT.newCall(t.getRequest()).enqueue(t.getHttpCallback()))
                    .count();
            latch = new CountDownLatch(nFiles);
            try {
                latch.await();
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        public class DownloadFileTask {
            private final Request request;
            private final Callback httpCallback;


            public DownloadFileTask(URL url, File file, Consumer<Return> callback) {
                request = new Request.Builder()
                        .get()
                        .url(url)
                        .build();

                httpCallback = new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.accept(new Return(
                                url,
                                file,
                                -1,
                                null,
                                e
                        ));
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream stream = response.body().byteStream();
                        FileUtils.copyInputStreamToFile(stream, file);
                        stream.close();
                        callback.accept(new Return(
                                url,
                                file,
                                response.code(),
                                response.message(),
                                null
                        ));
                        response.close();
                        latch.countDown();
                    }
                };
            }

            public Callback getHttpCallback() {
                return httpCallback;
            }

            public Request getRequest() {
                return request;
            }

            public class Return {
                private final URL url;
                private final File file;
                private final int responseCode;
                private final String responseMessage;
                private final IOException exception;

                private Return(URL url,
                               File file,
                               int responseCode,
                               String responseMessage,
                               IOException exception) {
                    this.url = url;
                    this.file = file;
                    this.responseCode = responseCode;
                    this.responseMessage = responseMessage;
                    this.exception = exception;
                }

                public URL getUrl() {
                    return url;
                }

                public File getFile() {
                    return file;
                }

                public int getResponseCode() {
                    return responseCode;
                }

                public String getResponseMessage() {
                    return responseMessage;
                }

                public IOException getException() {
                    return exception;
                }

                @Override
                public String toString() {
                    return "Return{" +
                            "url=" + url +
                            ", file=" + file +
                            ", responseCode=" + responseCode +
                            ", responseMessage='" + responseMessage + '\'' +
                            ", exception=" + exception +
                            '}';
                }
            }
        }

        public enum ExistingFileBehaviour {
            OVERWRITE,
            SKIP;
        }
    }
}
