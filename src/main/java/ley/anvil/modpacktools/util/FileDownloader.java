package ley.anvil.modpacktools.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FileDownloader {

    private FileDownloader() {
    }

    /**
     * Downloads files asynchronously
     *
     * @param files the files to download
     * @param nThreads how many worker threads to use
     * @param callback this callback will get the return code of each HTTP request and the file once a file has finished
     * @param tries how many times a download will be tried in case it fails
     * @param threadTimeout the time after which the download will be canceled
     * @param threadTimeoutUnit the unit of the threadTimeout time
     */
    public static void downloadAsync(Map<URL, File> files,
                                     int nThreads,
                                     Consumer<AsyncDownloader.DownloadFileTask.Return> callback,
                                     int tries,
                                     long threadTimeout,
                                     TimeUnit threadTimeoutUnit,
                                     int httpTimeout,
                                     AsyncDownloader.ExistingFileBehaviour existingFileBehaviour) {
        new AsyncDownloader(files,
                nThreads,
                callback,
                tries,
                threadTimeout,
                threadTimeoutUnit,
                httpTimeout,
                existingFileBehaviour);
    }

    public static class AsyncDownloader {
        ExecutorService service;
        Map<URL, File> files;
        Consumer<DownloadFileTask.Return> callback;
        int tries;
        long threadTimeout;
        TimeUnit threadTimeoutUnit;
        int httpTimeout;
        ExistingFileBehaviour existingFileBehaviour;

        private AsyncDownloader(Map<URL, File> files,
                                int nThreads,
                                Consumer<DownloadFileTask.Return> callback,
                                int tries,
                                long threadTimeout,
                                TimeUnit threadTimeoutUnit,
                                int httpTimeout,
                                ExistingFileBehaviour existingFileBehaviour) {
            this.files = files;
            this.callback = callback;
            this.tries = tries;
            this.threadTimeout = threadTimeout;
            this.threadTimeoutUnit = threadTimeoutUnit;
            this.service = Executors.newFixedThreadPool(nThreads);
            this.httpTimeout = httpTimeout;
            this.existingFileBehaviour = existingFileBehaviour;

            this.dispatchTasks();
        }

        private void dispatchTasks() {
            CompletableFuture.allOf(files.entrySet().stream()
                    .filter(f -> existingFileBehaviour == ExistingFileBehaviour.OVERWRITE || !f.getValue().exists())
                    .map(e -> CompletableFuture.supplyAsync(
                            new DownloadFileTask(e.getKey(), e.getValue(), tries), service)
                            .thenAccept(callback))
                    .toArray(CompletableFuture[]::new)).join();
            service.shutdown();
        }

        public class DownloadFileTask implements Supplier<DownloadFileTask.Return> {
            URL url;
            File file;
            int tries;

            private DownloadFileTask(URL url,
                                     File file,
                                     int tries) {
                this.url = url;
                this.file = file;
                this.tries = tries;
            }

            @Override
            public Return get() {
                IOException exception = null;
                String responseMessage = null;
                int responseCode = -1;

                while(tries > 0) {
                    try {
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.setReadTimeout(httpTimeout);
                        con.setConnectTimeout(httpTimeout);

                        con.connect();
                        responseCode = con.getResponseCode();
                        responseMessage = con.getResponseMessage();

                        file.getParentFile().mkdirs();
                        InputStream stream = con.getInputStream();
                        FileUtils.copyInputStreamToFile(stream, file);
                        stream.close();
                        break;
                    }catch(IOException e) {
                        exception = e;
                        tries--;
                    }
                }
                return new Return(url,
                        file,
                        responseCode,
                        responseMessage,
                        exception);
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
