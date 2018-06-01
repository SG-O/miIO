package device.vacuum;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class VacuumMap implements Serializable {
    private static final long serialVersionUID = 6328146796574327681L;
    private transient BufferedImage map;
    private transient List<Point2D.Float> path = new LinkedList<>();
    private Rectangle boundingBox;
    private int overSample;

    public VacuumMap(BufferedReader image, BufferedReader slam, int overSample) {
        if (overSample < 1) overSample = 1;
        this.overSample = overSample;
        this.map = new BufferedImage(1024 * overSample, 1024 * overSample, BufferedImage.TYPE_3BYTE_BGR);
        this.boundingBox = new Rectangle(1024 * overSample, 1024 * overSample);
        try {
            readMap(image);
            readSlam(slam);
        } catch (Exception ignored){
        }
    }

    private void readMap(BufferedReader image) throws IOException {
        int x = 0;
        int y = 0;
        int top = map.getHeight();
        int bottom = 0;
        int left = map.getWidth();
        int right = 0;
        if (image.readLine() == null) return;
        if (image.readLine() == null) return;
        while (true) {
            int[] rgb = {image.read(), image.read(), image.read()};
            if (rgb[0] < 0 || rgb[1] < 0 || rgb[2] < 0) {
                boundingBox = new Rectangle(left, top, (right - left) + 1, (bottom - top) + 1);
                return;
            }
            for (int i = 0; i < rgb.length; i++) {
                rgb[i] = rgb[i] & 0xFF;
            }
            for (int j = 0; j < overSample; j++) {
                for (int i = 0; i < overSample; i++) {
                    map.setRGB(x + i, y + j, new Color(rgb[0], rgb[1], rgb[2]).getRGB());
                }
            }
            if (rgb[0] != 125 || rgb[1] != 125 || rgb[2] != 125){
                if (x < left) left = x;
                if (x > right) right = x + (overSample - 1);
                if (y < top) top = y;
                if (y > bottom) bottom = y + (overSample - 1);
            }
            x += overSample;
            if (x >= map.getWidth()){
                x = 0;
                y += overSample;
            }
            if (y >= map.getHeight()){
                y = 0;
            }
        }
    }

    private void readSlam(BufferedReader slam) throws IOException {
        String line;
        boolean locked = true;
        while ((line = slam.readLine()) != null){
            if (line.contains("reset")){
                path = new LinkedList<>();
            }
            if (line.contains("lock")) locked = true;
            if (line.contains("unlock")) locked = false;
            if (locked) continue;
            if (line.contains("estimate")){
                String[] split = line.split("\\s+");
                if (split.length != 5) continue;
                float x;
                float y;
                try {
                    x = Float.valueOf(split[2]) * (20.0f * overSample);
                    y = Float.valueOf(split[3]) * (-20.0f * overSample);
                } catch (Exception e){
                    continue;
                }
                x += map.getWidth() / 2.0f;
                y += map.getHeight() / 2.0f;
                path.add(new Point2D.Float(x, y));
            }
        }
    }

    public BufferedImage getMap() {
        return map;
    }

    public List<Point2D.Float> getPath() {
        return path;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public BufferedImage getMapWithPathInBounds(){
        BufferedImage raw = getMapWithPath();
        return raw.getSubimage(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
    }

    public BufferedImage getMapWithPath() {
        BufferedImage pathMap = new BufferedImage(1024 * overSample, 1024 * overSample, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D img = pathMap.createGraphics();
        img.drawImage(map, null,0, 0);
        img.setBackground(Color.GREEN);
        img.setColor(Color.GREEN);
        int[] home = {(pathMap.getWidth() / 2) - (3 * overSample), (pathMap.getHeight() / 2) - (3 * overSample)};
        img.fillOval(home[0], home[1], 6 * overSample,6 * overSample);
        img.setColor(Color.BLUE);
        BasicStroke bs = new BasicStroke(1);
        img.setStroke(bs);
        Point2D.Float prev = null;
        for (Point2D.Float p : path){
            if (prev == null) {
                prev = p;
                continue;
            }
            img.drawLine((int)prev.x, (int)prev.y, (int)p.x, (int)p.y);
            prev = p;
        }
        return pathMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacuumMap vacuumMap = (VacuumMap) o;
        if (map.getWidth() != vacuumMap.map.getWidth() || map.getHeight() != vacuumMap.map.getHeight()) return false;
        int w = map.getWidth();
        int h = map.getHeight();
        for (int j = 0; j < h; j++){
            for (int i = 0; i < w; i++){
                if (map.getRGB(i, j) != vacuumMap.map.getRGB(i, j)) return false;
            }
        }
        return overSample == vacuumMap.overSample &&
                Objects.equals(path, vacuumMap.path) &&
                Objects.equals(boundingBox, vacuumMap.boundingBox);
    }

    @Override
    public int hashCode() {

        return Objects.hash(map.getHeight(), map. getWidth(), path, boundingBox, overSample);
    }

    @Override
    public String toString() {
        return "VacuumMap{" +
                "map=width:" + map.getWidth() + "; height:" + map.getHeight() +
                ", pathEntries=" + path.size() +
                ", boundingBox=" + boundingBox +
                ", overSample=" + overSample +
                '}';
    }

    private byte[] mapToBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(this.map, "png", baos );
        baos.flush();
        byte[] imgInByte = baos.toByteArray();
        baos.close();
        return imgInByte;
    }

    private void bytesToMap(byte[] source) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(source);
        this.map = ImageIO.read(bais);
        if (this.map == null) {
            this.map = new BufferedImage(1024 * overSample, 1024 * overSample, BufferedImage.TYPE_3BYTE_BGR);
        }
    }

    private byte[] pathToBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this.path);
        oos.flush();
        baos.flush();
        byte[] outputTrimmed = baos.toByteArray();
        oos.close();
        baos.close();
        return outputTrimmed;
    }

    private void bytesToPath(byte[] source) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(source);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        try {
            //noinspection unchecked
            this.path = (List<Point2D.Float>) o;
        } catch (ClassCastException e){
            throw new IOException("Cant convert to path");
        }
        ois.close();
        bais.close();
    }

    private byte[] compress(byte[] data) {
        byte[] output = new byte[data.length + 1000];
        Deflater compressor = new Deflater();
        compressor.setInput(data);
        compressor.finish();

        byte[] outputTrimmed = new byte[compressor.deflate(output)];
        System.arraycopy(output, 0, outputTrimmed, 0, outputTrimmed.length);
        return outputTrimmed;
    }

    private void inflate(byte[] compressed, byte[] restored) throws IOException {
        Inflater inflate = new Inflater();
        inflate.setInput(compressed);
        inflate.finished();
        try {
            int restoredBytes = inflate.inflate(restored);
            if (restoredBytes != restored.length) throw new IOException("Image size does not match");
        } catch (DataFormatException e) {
            throw new IOException("Inflation failed");
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        byte[] imgBytes = mapToBytes();
        byte[] compressedMap = compress(imgBytes);
        byte[] pathBytes = pathToBytes();
        byte[] compressedPath = compress(pathBytes);
        out.writeInt(compressedMap.length);
        out.writeInt(imgBytes.length);
        out.writeInt(compressedPath.length);
        out.writeInt(pathBytes.length);
        out.write(compressedMap);
        out.write(compressedPath);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        byte[] compressedMap = new byte[in.readInt()];
        byte[] mapBytes = new byte[in.readInt()];
        byte[] compressedPath = new byte[in.readInt()];
        byte[] pathBytes = new byte[in.readInt()];
        in.readFully(compressedMap);
        in.readFully(compressedPath);

        inflate(compressedMap, mapBytes);
        inflate(compressedPath, pathBytes);

        bytesToMap(mapBytes);
        bytesToPath(pathBytes);
    }
}
