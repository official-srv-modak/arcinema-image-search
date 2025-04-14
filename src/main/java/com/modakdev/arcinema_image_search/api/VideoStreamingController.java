package com.modakdev.arcinema_image_search.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

@Controller
public class VideoStreamingController {

    @Value("${video.base.path}")
    private String videoBasePath;

    @GetMapping("/stream/{movieName}")
    @ResponseBody
    public ResponseEntity<Resource> streamVideo(@PathVariable String movieName,
                                               @RequestHeader(value = "Range", required = false) String range) throws IOException {
        try {
            File videoFile = new File(videoBasePath, movieName + ".mp4"); // Assuming .mp4 extension
            if (!videoFile.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found");
            }

            Resource videoResource = new FileSystemResource(videoFile);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("video/mp4")); // Adjust MIME type based on your video format

            if (range != null) {
                long fileSize = videoResource.contentLength();
                long start = 0;
                long end = fileSize - 1;

                if (range.startsWith("bytes=")) {
                    String[] parts = range.substring(6).split("-");
                    if (parts.length > 0) {
                        try {
                            start = Long.parseLong(parts[0]);
                        } catch (NumberFormatException e) {
                            start = 0;
                        }
                        if (parts.length > 1) {
                            try {
                                end = Long.parseLong(parts[1]);
                            } catch (NumberFormatException e) {
                                end = fileSize - 1;
                            }
                        }
                    }
                }

                if (start < 0 || end >= fileSize || start > end) {
                    headers.add("Content-Range", "bytes */" + fileSize);
                    return new ResponseEntity<>(null, headers, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
                }

                long contentLength = end - start + 1;
                headers.add("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
                headers.setContentLength(contentLength);
                return new ResponseEntity<>(new PartialResource(videoResource, start, contentLength), headers, HttpStatus.PARTIAL_CONTENT);
            } else {
                headers.setContentLength(videoResource.contentLength());
                return new ResponseEntity<>(videoResource, headers, HttpStatus.OK);
            }

        } catch (IOException e) {
            // Log the error properly
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while streaming video", e);
        }
    }

    // Helper class for partial content streaming
    private static class PartialResource implements Resource {
        private final Resource resource;
        private final long start;
        private final long length;

        public PartialResource(Resource resource, long start, long length) {
            this.resource = resource;
            this.start = start;
            this.length = length;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            InputStream inputStream = resource.getInputStream();
            inputStream.skip(start);
            return new LimitedInputStream(inputStream, length);
        }

        @Override
        public boolean exists() {
            return resource.exists();
        }

        @Override
        public boolean isReadable() {
            return resource.isReadable();
        }

        @Override
        public boolean isOpen() {
            return resource.isOpen();
        }

        @Override
        public java.net.URL getURL() throws IOException {
            return resource.getURL();
        }

        @Override
        public java.net.URI getURI() throws IOException {
            return resource.getURI();
        }

        @Override
        public File getFile() throws IOException {
            return resource.getFile();
        }

        @Override
        public long contentLength() throws IOException {
            return length;
        }

        @Override
        public long lastModified() throws IOException {
            return resource.lastModified();
        }

        @Override
        public Resource createRelative(String path) throws IOException {
            return resource.createRelative(path);
        }

        @Override
        public String getFilename() {
            return resource.getFilename();
        }

        @Override
        public String getDescription() {
            return resource.getDescription();
        }
    }

    // Helper class to limit the input stream
    private static class LimitedInputStream extends InputStream {
        private final InputStream delegate;
        private long remaining;

        public LimitedInputStream(InputStream delegate, long length) {
            this.delegate = delegate;
            this.remaining = length;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int read = delegate.read();
            if (read != -1) {
                remaining--;
            }
            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int read = delegate.read(b, off, (int) Math.min(len, remaining));
            if (read > 0) {
                remaining -= read;
            }
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = delegate.skip(Math.min(n, remaining));
            remaining -= skipped;
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(delegate.available(), remaining);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }
    }
}