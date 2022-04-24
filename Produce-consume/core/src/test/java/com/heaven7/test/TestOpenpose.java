package com.heaven7.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestOpenpose {

    public static void main(String[] args) {
        int[] xx = new int[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47};
        int[] yy = rollAxis(xx, new int[]{4, 4, 3}, 2);
        String ss = "";
        for (int i = 0; i < yy.length; i++) {
            ss += yy[i] + " ";
        }
        System.out.println(ss);
    }

    @Test
    public void test1() {
        printArray(getSubShape(new int[]{5, 4, 3}, 0));
        printArray(getSubShape(new int[]{5, 4, 3}, 1));
        printArray(getSubShape(new int[]{5, 4, 3}, 2));
    }
    @Test
    public void test2(){
        Integer[] xx = new Integer[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
                40, 41, 42, 43, 44, 45, 46, 47};
        int[] shape = new int []{4, 4, 3};

        List out = new ArrayList<>();
        IntChunk chunk = new IntChunk(Arrays.asList(xx));
        parse(chunk, shape[0], getSubShape(shape, 0), out);
        System.out.println(out);
    }
    private static void parse(Chunk in, int count, int[] subShape,List out){
        if(count <= 1 || out == null || subShape == null){
            return;
        }
        int[] ss = getSubShape(subShape, 0);
        if(subShape != null){
            for (int i = 0 ; i < count ; i ++){
                out.add(new ArrayList<Chunk>());
            }
            List<Chunk> list = in.parse(count);
            for (int k = 0; k < list.size() ; k ++){
                Chunk c = list.get(k);
                parse(c, subShape[0], ss, (List) out.get(k));
            }
        }else {
            out.addAll(in.parse(count));
        }
    }
    private static void printArray(int [] arr){
        System.out.println(Arrays.toString(arr));
    }

    private static int[] rollAxis(int[] input, int[] size, int axis) {
        int[] o = new int[size[0] * size[1] * size[2]];
        int i = 0;
        for (int k = 0; k < size[axis]; k++) {
            for (int j = 0; j < input.length; j += size[axis]) {
                o[i++] = input[k + j];
            }
        }
        return o;
    }

    private static int[] getSubShape(int[] shape, int curIndex) {
        int expect = shape.length - curIndex - 1;
        if (expect <= 0) {
            return null;
        }
        int[] a = new int[expect];
        for (int i = curIndex + 1; i < shape.length; i++) {
            a[i - curIndex - 1] = shape[i];
        }
        return a;
    }

    //layerIndex层数
    private static int computeSubCount(int[] shape, int layerIndex){
        int c = 1;
        for (int i = layerIndex + 1; i < shape.length; i++) {
            c *= shape[i];
        }
        return c;
    }

    public interface Chunk{
        List<Chunk> parse(int everyCount);
    }
    private static class IntChunk implements Chunk{
        List<Integer> arr;
        public IntChunk(List<Integer> arr) {
            this.arr = arr;
        }
        @Override
        public List<Chunk> parse(int groupCount) {
            System.out.println(String.format("(arr.size, groupCount) = (%d, %d)", arr.size(), groupCount));
            Assert.assertTrue(arr.size() % groupCount == 0);
            List<Chunk> out = new ArrayList<>();
            int c = arr.size() / groupCount;
            for (int i = 0 ; i < groupCount ; i ++){
                IntChunk chunk = new IntChunk(new ArrayList<Integer>());
                for (int k = 0 ; k < c ; k ++){
                    chunk.arr.add(arr.get(i * c + k));
                }
                out.add(chunk);
            }
            return out;
        }
    }
}
