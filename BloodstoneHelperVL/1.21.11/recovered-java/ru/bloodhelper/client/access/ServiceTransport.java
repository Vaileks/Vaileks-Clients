/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.access;

import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

final class ServiceTransport
implements AutoCloseable {
    static final URI ORIGIN = URI.create("https://178.22.24.133");
    static final int JSON_LIMIT = 262144;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(HttpClient.Redirect.NEVER).build();

    ServiceTransport() {
    }

    byte[] request(String string, String string2, String string3, JsonObject jsonObject, int n) throws IOException {
        throw new IOException("Service unavailable");
    }

    static URI target(String path) throws IOException {
        if (path == null || !path.startsWith("/v1/") || path.indexOf(35) >= 0 || path.indexOf(92) >= 0 || path.contains("..")) {
            throw new IOException("Invalid service path");
        }
        try {
            URI target = ORIGIN.resolve(path);
            if (!"https".equals(target.getScheme()) || !ORIGIN.getRawAuthority().equals(target.getRawAuthority()) || target.getUserInfo() != null) {
                throw new IOException("Foreign service origin");
            }
            return target;
        }
        catch (IllegalArgumentException malformed) {
            throw new IOException("Invalid service URI", malformed);
        }
    }

    @Override
    public void close() {
        this.client.shutdownNow();
    }

    private static /* synthetic */ HttpResponse.BodySubscriber lambda$request$0(int maximum, HttpResponse.ResponseInfo info) {
        return new BoundedBody(maximum);
    }

    static final class Failure
    extends IOException {
        final int status;

        Failure(int status) {
            super("Service HTTP " + status);
            this.status = status;
        }
    }

    static final class BoundedBody
    implements HttpResponse.BodySubscriber<byte[]> {
        private final CompletableFuture<byte[]> completion = new CompletableFuture();
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final int maximum;
        private Flow.Subscription subscription;

        BoundedBody(int maximum) {
            this.maximum = maximum;
        }

        @Override
        public CompletionStage<byte[]> getBody() {
            return this.completion;
        }

        @Override
        public void onSubscribe(Flow.Subscription next) {
            if (this.subscription != null) {
                next.cancel();
                return;
            }
            this.subscription = next;
            next.request(1L);
        }

        @Override
        public void onNext(List<ByteBuffer> items) {
            if (this.completion.isDone()) {
                return;
            }
            for (ByteBuffer buffer : items) {
                if (buffer.remaining() > this.maximum - this.bytes.size()) {
                    this.subscription.cancel();
                    this.completion.completeExceptionally(new IOException("Service response too large"));
                    return;
                }
                byte[] copy = new byte[buffer.remaining()];
                buffer.get(copy);
                this.bytes.writeBytes(copy);
            }
            this.subscription.request(1L);
        }

        @Override
        public void onError(Throwable error) {
            this.completion.completeExceptionally(error);
        }

        @Override
        public void onComplete() {
            this.completion.complete(this.bytes.toByteArray());
        }
    }
}

