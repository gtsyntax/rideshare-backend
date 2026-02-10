package com.rideshare.rideshare_backend.util;

/**
 * Geohash encoding and decoding utility
 * Converts lat/lon coordinates to geohash strings and vice versa
 */
public class GeohashUtil {
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int DEFAULT_PRECISION = 6;

    /**
     * Encode latitude and longitude to geohash
     *
     * @param latitude Latitude (-90 to 90)
     * @param longitude Longitude (-180 to 180)
     * @param precision Length of geohash (higher = more precise)
     * @return Geohash string
     */
    public static String encode(double latitude, double longitude, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};

        StringBuilder geohash = new StringBuilder();
        StringBuilder bits = new StringBuilder();

        boolean isEven = true;

        while(geohash.length() < precision) {
            if (isEven) {
                double mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude > mid) {
                    bits.append('1');
                    lonRange[0] = mid;
                } else {
                    bits.append('0');
                    lonRange[1] = mid;
                }
            } else {
                double mid = (latRange[0] + latRange[1]) / 2;
                if (latitude > mid) {
                    bits.append('1');
                    latRange[0] = mid;
                } else {
                    bits.append('0');
                    latRange[1] = mid;
                }
            }

            isEven = !isEven;

            if (bits.length() == 5) {
                int index = Integer.parseInt(bits.toString(), 2);
                geohash.append(BASE32.charAt(index));
                bits = new StringBuilder();
            }
        }

        return geohash.toString();
    }

    public static String encode(double latitude, double longitude) {
        return encode(latitude, longitude, DEFAULT_PRECISION);
    }

    /**
     * Decode geohash to approximate latitude and longitude
     * Returns the center point of the geohash cell
     *
     * @param geohash Geohash string
     * @return Array [latitude, longitude]
     */
    public static double[] decode(String geohash) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};

        boolean isEven = true;

        for (char c : geohash.toCharArray()) {
            int index = BASE32.indexOf(c);

            if (index == -1) {
                throw new IllegalArgumentException("Invalid geohash character: " + c);
            }

            for (int mask = 16; mask > 0; mask >>= 1) {
                if (isEven) {
                    double mid = (lonRange[0] + lonRange[1]) / 2;
                    if ((index & mask) != 0) {
                        lonRange[0] = mid;
                    } else {
                        lonRange[1] = mid;
                    }
                } else {
                    double mid = (latRange[0] + latRange[1]) / 2;
                    if ((index & mask) != 0) {
                        latRange[0] = mid;
                    } else {
                        latRange[1] = mid;
                    }
                }

                isEven = !isEven;
            }
        }

        double lat = (latRange[0] + latRange[1]) / 2;
        double lon = (lonRange[0] + lonRange[1]) / 2;

        return new double[]{lat, lon};
    }

    public static String[] getNeighbors(String geohash) {
        return new String[0];
    }

    public static double getPrecisionInKm(int precision) {
        double[] widths = {
                5000,    // precision 1: ±2500 km
                1250,    // precision 2: ±630 km
                156,     // precision 3: ±78 km
                39.1,    // precision 4: ±20 km
                4.9,     // precision 5: ±2.4 km
                1.2,     // precision 6: ±0.61 km
                0.153,   // precision 7: ±0.076 km
                0.0382   // precision 8: ±0.019 km
        };

        if (precision < 1 || precision > 8) {
            throw new IllegalArgumentException("Precision must be between 1 and 8");
        }

        return widths[precision - 1];
    }
}
