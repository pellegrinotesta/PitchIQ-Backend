package com.pitchiq.bff.PitchIQ.tracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchiq.bff.PitchIQ.tracking.service.VideoUrlDownloader;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VideoUrlDownloaderTest {

    private final VideoUrlDownloader sicuro = new VideoUrlDownloader(new ObjectMapper());
    private HttpServer server;
    private String base;

    @TempDir
    Path tmp;

    @BeforeEach
    void avviaServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/clip.mp4", ex -> {
            byte[] body = new byte[2048];
            ex.getResponseHeaders().add("Content-Type", "video/mp4");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.createContext("/pagina", ex -> {
            byte[] body = "<html></html>".getBytes();
            ex.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.createContext("/redirect", ex -> {
            ex.getResponseHeaders().add("Location", "/clip.mp4");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void fermaServer() {
        server.stop(0);
    }

    @Test
    void springRiesceACreareIlBean() {
        try (var ctx = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            ctx.registerBean(ObjectMapper.class);
            ctx.registerBean(VideoUrlDownloader.class);
            ctx.refresh();
            assertThat(ctx.getBean(VideoUrlDownloader.class)).isNotNull();
        }
    }

    @Test
    void rifiutaYoutubeConMessaggioChiaro() {
        assertThatThrownBy(() -> sicuro.scarica("https://www.youtube.com/watch?v=abc", tmp.resolve("s")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> {
                    var rse = (ResponseStatusException) e;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(rse.getReason()).contains("YouTube");
                });
        assertThatThrownBy(() -> sicuro.scarica("https://youtu.be/abc", tmp.resolve("s")))
                .hasMessageContaining("YouTube");
    }

    @Test
    void rifiutaSchemiNonHttpEIndirizziInterni() {
        assertThatThrownBy(() -> sicuro.scarica("ftp://example.com/a.mp4", tmp.resolve("s")))
                .hasMessageContaining("http");
        assertThatThrownBy(() -> sicuro.scarica("file:///etc/passwd", tmp.resolve("s")))
                .hasMessageContaining("http");
        assertThatThrownBy(() -> sicuro.scarica(base + "/clip.mp4", tmp.resolve("s")))
                .hasMessageContaining("non consentito");
        assertThatThrownBy(() -> sicuro.scarica("http://169.254.169.254/latest/meta-data", tmp.resolve("s")))
                .hasMessageContaining("non consentito");
        assertThatThrownBy(() -> sicuro.scarica("http://10.0.0.5/a.mp4", tmp.resolve("s")))
                .hasMessageContaining("non consentito");
    }

    @Test
    void scaricaUnFileVideoDirettoSeguendoIRedirect() throws Exception {
        var downloader = permissivo();
        Path dest = tmp.resolve("source");

        var risultato = downloader.scarica(base + "/redirect", dest);

        assertThat(Files.size(dest)).isEqualTo(2048);
        assertThat(risultato.byteScaricati()).isEqualTo(2048);
    }

    @Test
    void spiegaQuandoIlLinkEUnaPaginaWeb() throws Exception {
        assertThatThrownBy(() -> permissivo().scarica(base + "/pagina", tmp.resolve("s")))
                .hasMessageContaining("pagina web");
    }

    /** Il costruttore che accetta il filtro sugli indirizzi è package-private: nei test si sblocca il loopback. */
    private static VideoUrlDownloader permissivo() throws Exception {
        Constructor<VideoUrlDownloader> c =
                VideoUrlDownloader.class.getDeclaredConstructor(ObjectMapper.class, Predicate.class);
        c.setAccessible(true);
        Predicate<InetAddress> tutti = a -> true;
        return c.newInstance(new ObjectMapper(), tutti);
    }
}
