/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.jcraft.jogg.Packet
 *  com.jcraft.jogg.Page
 *  com.jcraft.jogg.StreamState
 *  com.jcraft.jogg.SyncState
 *  com.jcraft.jorbis.Block
 *  com.jcraft.jorbis.Comment
 *  com.jcraft.jorbis.DspState
 *  com.jcraft.jorbis.Info
 */
package ru.bloodhelper.client.music;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

final class VorbisPcmStream
extends InputStream {
    private final InputStream input;
    private final SyncState sync = new SyncState();
    private final StreamState stream = new StreamState();
    private final Page page = new Page();
    private final Packet packet = new Packet();
    private final Info info = new Info();
    private final Comment comment = new Comment();
    private final DspState dsp = new DspState();
    private final Block block = new Block(this.dsp);
    private final float[][][] samples = new float[1][][];
    private int[] offsets;
    private byte[] decoded;
    private int cursor;
    private int length;
    private int serial;
    private boolean eof;
    private boolean closed;
    private boolean endPage;

    private VorbisPcmStream(File file) throws IOException {
        this.input = new BufferedInputStream(new FileInputStream(file));
        try {
            this.sync.init();
            this.info.init();
            this.comment.init();
            if (!this.nextPage()) {
                throw new IOException("Missing Ogg header");
            }
            this.serial = this.page.serialno();
            this.stream.init(this.serial);
            if (this.stream.pagein(this.page) < 0) {
                throw new IOException("Invalid Ogg page");
            }
            for (int h = 0; h < 3; ++h) {
                if (this.nextPacket() && this.info.synthesis_headerin(this.comment, this.packet) >= 0) continue;
                throw new IOException("Invalid Vorbis header");
            }
            if (this.info.channels < 1 || this.info.channels > 2 || this.info.rate < 8000 || this.info.rate > 192000) {
                throw new IOException("Unsupported Vorbis format");
            }
            if (this.dsp.synthesis_init(this.info) != 0) {
                throw new IOException("Invalid Vorbis setup");
            }
            this.block.init(this.dsp);
            this.offsets = new int[this.info.channels];
            this.decoded = new byte[4096 * this.info.channels * 2];
        }
        catch (IOException | RuntimeException failure) {
            this.input.close();
            throw failure;
        }
    }

    static AudioInputStream open(File file, long durationMs) throws IOException {
        VorbisPcmStream decoder = new VorbisPcmStream(file);
        AudioFormat format = new AudioFormat(decoder.info.rate, 16, decoder.info.channels, true, false);
        long frames = durationMs > 0L ? durationMs * (long)decoder.info.rate / 1000L : -1L;
        return new AudioInputStream(decoder, format, frames);
    }

    private boolean nextPage() throws IOException {
        int found;
        while ((found = this.sync.pageout(this.page)) != 1) {
            if (found < 0) {
                throw new IOException("Corrupt Ogg stream");
            }
            if (this.eof) {
                return false;
            }
            int at = this.sync.buffer(4096);
            int read = this.input.read(this.sync.data, at, 4096);
            if (read < 0) {
                this.eof = true;
                continue;
            }
            this.sync.wrote(read);
        }
        return true;
    }

    private boolean nextPacket() throws IOException {
        do {
            int result;
            if ((result = this.stream.packetout(this.packet)) == 1) {
                return true;
            }
            if (result < 0) {
                throw new IOException("Corrupt Vorbis packet");
            }
            if (this.endPage || !this.nextPage()) {
                return false;
            }
            if (this.page.serialno() != this.serial) {
                throw new IOException("Chained Vorbis streams are not supported");
            }
            boolean bl = this.endPage = this.page.eos() != 0;
        } while (this.stream.pagein(this.page) >= 0);
        throw new IOException("Invalid Vorbis page");
    }

    private boolean fill() throws IOException {
        while (!this.closed) {
            int count = this.dsp.synthesis_pcmout(this.samples, this.offsets);
            if (count > 0) {
                count = Math.min(count, this.decoded.length / (this.info.channels * 2));
                for (int frame = 0; frame < count; ++frame) {
                    for (int channel = 0; channel < this.info.channels; ++channel) {
                        int value = Math.clamp((long)((int)(this.samples[0][channel][this.offsets[channel] + frame] * 32767.0f)), Short.MIN_VALUE, Short.MAX_VALUE);
                        int index = (frame * this.info.channels + channel) * 2;
                        this.decoded[index] = (byte)value;
                        this.decoded[index + 1] = (byte)(value >>> 8);
                    }
                }
                this.dsp.synthesis_read(count);
                this.cursor = 0;
                this.length = count * this.info.channels * 2;
                return true;
            }
            if (!this.nextPacket()) {
                return false;
            }
            if (this.block.synthesis(this.packet) != 0) {
                throw new IOException("Invalid Vorbis audio packet");
            }
            this.dsp.synthesis_blockin(this.block);
        }
        return false;
    }

    @Override
    public int read() throws IOException {
        if (this.cursor == this.length && !this.fill()) {
            return -1;
        }
        return this.decoded[this.cursor++] & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int offset, int count) throws IOException {
        Objects.checkFromIndexSize(offset, count, bytes.length);
        if (count == 0) {
            return 0;
        }
        if (this.cursor == this.length && !this.fill()) {
            return -1;
        }
        int copied = Math.min(count, this.length - this.cursor);
        System.arraycopy(this.decoded, this.cursor, bytes, offset, copied);
        this.cursor += copied;
        return copied;
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            this.input.close();
        }
        finally {
            this.block.clear();
            this.dsp.clear();
            this.info.clear();
            this.stream.clear();
            this.sync.clear();
        }
    }
}

