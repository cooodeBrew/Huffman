
import java.io.*;
import java.util.Map;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private int[] frequency;
    private Map<Integer, String> bitMap;
    private HuffmanCodeTree tree;
    private int saveBits;
    private int compressBits;
    private int hf;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        // Step1 : Count frequencies
        frequency = countFrequencies(in);
        hf = headerFormat;

        // Step2 : Build HuffmanCodeTree base on frequencies
        tree = new HuffmanCodeTree(frequency);
        bitMap = tree.createBitCodeMap();

        // step3: calculate the number of bits required
        //        to store the compressed data
        int originBits = getOriginalBits(frequency);
        compressBits = getCompressBits(frequency, bitMap);
        // add twice for magic number and headFormat
        compressBits += IHuffConstants.BITS_PER_INT;
        compressBits += IHuffConstants.BITS_PER_INT;

        // add the number of bits of the header data
        if (headerFormat == STORE_COUNTS) {
            compressBits += IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
        } else if (headerFormat == STORE_TREE) {
            compressBits += tree.getTreeBits() + IHuffConstants.BITS_PER_INT;
        }
        // add the PSEUDO_EOF
        compressBits += bitMap.get(IHuffConstants.PSEUDO_EOF).length();
        saveBits = originBits - compressBits;
        return saveBits;

//        showString("Not working yet");
//        myViewer.update("Still not working");
//        throw new IOException("preprocess not implemented");
    }

    private int getCompressBits(int[] freq, Map<Integer, String > map) {
        int sum = 0;
        for (int k : map.keySet()) {
            if (k != IHuffConstants.PSEUDO_EOF) {
                String s = map.get(k);
                int f = freq[k];
                sum += f * s.length();
            }
        }
        return sum;
    }

    private int getOriginalBits(int[] freq) {
        int sum = 0;
        for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
            sum += freq[i] * IHuffConstants.BITS_PER_WORD;
        }
        return sum;
    }

    private int[] countFrequencies(InputStream in) throws IOException {
        int[] freq = new int[ALPH_SIZE];
        // Loop through each character in the input stream
        int nextChar;
        while ((nextChar = in.read()) != -1) {
            // Increment the frequency of the character
            freq[nextChar]++;
        }
        return freq;
    }



    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
//        throw new IOException("compress is not implemented");

        if (saveBits > 0 || force) {
            BitOutputStream os = new BitOutputStream(out);

            // the int after magic int must be the headFormat
            os.writeBits(BITS_PER_INT, MAGIC_NUMBER);
            // write the sign for compress file
            os.writeBits(BITS_PER_INT, hf);

            if (hf == STORE_COUNTS) {
                for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
                    os.writeBits(BITS_PER_INT, frequency[i]);
                }
            } else if (hf == STORE_TREE) {
                os.writeBits(BITS_PER_INT, tree.getSize());
                tree.preOrderTraverse(os);
            }
            BitInputStream is = new BitInputStream(in);
            int current = is.readBits(BITS_PER_WORD);

            while (current != -1) {
                String path = bitMap.get(current);
                for (int i = 0; i < path.length(); i++) {
                    os.writeBits(1, Character.getNumericValue(path.charAt(i)));
                }
                current = is.readBits(BITS_PER_WORD);
            }

            // Close input and output streams
            is.close();
            os.close();

        }
        return compressBits;
    }


    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
//	        throw new IOException("uncompress not implemented");
        BitOutputStream os = new BitOutputStream(out);
        BitInputStream is = new BitInputStream(in);
        // get the magic number and headFormat
        int magicNum = is.readBits(BITS_PER_INT);
        int headFormat = is.readBits(BITS_PER_INT);

        // check magic number to test if the file can be uncompressed
        if (magicNum != MAGIC_NUMBER) {
            myViewer.showError("The file can't be uncompressed.");
            os.close();
            is.close();
            return -1;
        }

        if (headFormat == STORE_COUNTS) {
            // rebuild frequency
            int[] frequency = new int[ALPH_SIZE];
            int item;
            for(int i = 0; i < ALPH_SIZE; i++){
                item = is.readBits(BITS_PER_INT);
                frequency[i] = item;
            }
        } else if (headFormat == STORE_TREE) {
            // read the size of tree
            is.readBits(BITS_PER_INT);
            tree = new HuffmanCodeTree(is);
        }

        os.close();
        is.close();
        return 0;
    }


    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s){
        if (myViewer != null) {
            myViewer.update(s);
        }
    }


}
