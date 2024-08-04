package com.shengsheng.pixlate_image.color_combine;

import android.util.Log;
import android.util.Pair;

import com.shengsheng.pixlate_image.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class color_combine<V> {
    private static final String TAG = "Color Combine";

    /** maximum distance of values to be considered as cluster */
    private double epsilon = 1f;

    /** minimum number of members to consider cluster */
    private int minimumNumberOfClusterMembers = 2;

    /** distance metric applied for clustering **/
    private color_dif_method color_diff = null;

    /** internal list of input values to be clustered */
    private float[][][] inputValues = null;

    /** index maintaining visited points */
    private int[][] visitedPoints = null;
    Object vp_lock = new Object();

    private MainActivity.control_pixlate con_pix_thread = null;

    private int input_pixel_size = 0;
    private int w = 0;
    private int h = 0;

    private float[][][] resultList = null;
    private int color_group = 0;
    Object resultList_lock = new Object();

    public color_combine(final float[][][] inputValues, int minNumElements, double maxDistance, color_dif_method color_diff, MainActivity.control_pixlate con_pix_thread) throws color_combine_exception {
        setInputValues(inputValues);
        setMinimalNumberOfMembersForCluster(minNumElements);
        setMaximalDistanceOfClusterMembers(maxDistance);
        set_color_dif_method(color_diff);
        set_con_pix_thread(con_pix_thread);
        reset();

    }

    private void reset() {
        color_group = 0;
        resultList = inputValues;
        visitedPoints = new int[w][h];
    }

    private void set_con_pix_thread(MainActivity.control_pixlate con_pix_thread) {
        this.con_pix_thread = con_pix_thread;
    }

    public void set_color_dif_method (final color_dif_method color_diff) throws color_combine_exception {
        if (color_diff == null) {
            throw new color_combine_exception("Color Combine: Distance metric has not been specified (null).");
        }
        this.color_diff = color_diff;
    }

    public void setInputValues(final float[][][] collection) throws color_combine_exception {
        if (collection == null) {
            throw new color_combine_exception("Color Combine: List of input values is null.");
        }
        this.inputValues = collection;
        this.w = inputValues.length;
        this.h = inputValues[0].length;
        this.input_pixel_size = w*h;
    }

    public void setMinimalNumberOfMembersForCluster(final int minimalNumberOfMembers) {
        this.minimumNumberOfClusterMembers = minimalNumberOfMembers;
    }

    public void setMaximalDistanceOfClusterMembers(final double maximalDistance) {
        this.epsilon = maximalDistance;
    }

    public float[][][] get_color_group() throws color_combine_exception {

        if (inputValues == null) {
            throw new color_combine_exception("Color Combine: List of input values is null.");
        }

        if (inputValues.length == 0 || inputValues[0].length == 0) {
            throw new color_combine_exception("Color Combine: List of input values is empty.");
        }

        if (input_pixel_size < 2) {
            throw new color_combine_exception("Color Combine: Less than two input values cannot be clustered. Number of input values: " + input_pixel_size);
        }

        if (epsilon < 0) {
            throw new color_combine_exception("Color Combine: Maximum distance of input values cannot be negative. Current value: " + epsilon);
        }

        if (minimumNumberOfClusterMembers < 2) {
            throw new color_combine_exception("Color Combine: Clusters with less than 2 members don't make sense. Current value: " + minimumNumberOfClusterMembers);
        }

        ArrayList<Thread> T_list = new ArrayList<>();
        int thread_num = 1;
        for(int i = 1; i <= thread_num; i++){
            String thread_name = "Group Color Thread # "+i;
            Group t = new Group(thread_name);
            t.start();
            T_list.add(t);
        }
        for(int i = 1; i <= thread_num; i++){
            try {
                T_list.get(i-1).join();
            } catch (InterruptedException e) {
                Log.e(TAG, "thread join fail"+e);
            }
        }

        Log.d(TAG, ": finished, and get "+ color_group +" groups");

        return resultList;
    }


    public class Group extends Thread{
        private String T_name;

        public Group(String thread_name) {
            T_name = thread_name;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */

        @Override
        public void run() {
            group_proc();
        }

        public void group_proc(){
            while (!con_pix_thread.pix_isinterrupt()) {
                int[] one_pixel = Get_unscan_pixel();
                if(one_pixel == null){
                    Log.d(TAG, T_name+"can not get one pixel: ");
                    return;
                }
                ArrayList<int[]> neighbours = null;
                try {
                    neighbours = getNeighbours(inputValues[one_pixel[0]][one_pixel[1]]);
                } catch (com.shengsheng.pixlate_image.color_combine.color_combine_exception color_combine_exception) {
                    Log.e(TAG, color_combine_exception.toString());
                }
                ArrayList<int[]> deal_neighbours = new ArrayList<>();
                double total_r = 0;
                double total_g = 0;
                double total_b = 0;
                double total_a = 0;
                for(int i = 0; i < neighbours.size(); i++){
                    int[] one_neighbour_pixel = neighbours.get(i);
                    synchronized(vp_lock){
                        if (visitedPoints[one_neighbour_pixel[0]][one_neighbour_pixel[1]] == 0) {
                            visitedPoints[one_neighbour_pixel[0]][one_neighbour_pixel[1]] = 1;
                            deal_neighbours.add(one_neighbour_pixel);
                            total_r += inputValues[one_neighbour_pixel[0]][one_neighbour_pixel[1]][0];
                            total_g += inputValues[one_neighbour_pixel[0]][one_neighbour_pixel[1]][1];
                            total_b += inputValues[one_neighbour_pixel[0]][one_neighbour_pixel[1]][2];
                            total_a += inputValues[one_neighbour_pixel[0]][one_neighbour_pixel[1]][3];
                        }
                    }
                }
                float average_r = (float)(total_r / deal_neighbours.size());
                float average_g = (float)(total_g / deal_neighbours.size());
                float average_b = (float)(total_b / deal_neighbours.size());
                float average_a = (float)(total_a / deal_neighbours.size());
                float[] avg_col = new float[]{average_r, average_g, average_b, average_a};

                for(int i = 0; i < deal_neighbours.size(); i++){
                    int[] one_deal_neighbours_pixel = deal_neighbours.get(i);
                    synchronized (resultList_lock){
                        resultList[one_deal_neighbours_pixel[0]][one_deal_neighbours_pixel[1]] = avg_col;
                    }
                }
                Log.d(TAG, T_name+"Get one group");
                dealed_p_copy();
            }
        }

        private synchronized void dealed_p_copy(){
            color_group++;
        }

        private int[] Get_unscan_pixel(){
            int[] ans = null;
            for(int w_index = 0; w_index < w; w_index++){
                for(int h_index = 0; h_index < h; h_index++){
                    synchronized(vp_lock){
                        if (visitedPoints[w_index][h_index] == 0) {
                            ans = new int[]{w_index, h_index};
                            visitedPoints[w_index][h_index] = 1;
                            return ans;
                        }
                    }
                }
            }
            return ans;
        }
    }


    private ArrayList<int[]> getNeighbours(float[] inputValue) throws color_combine_exception {

//        Log.d(TAG,"get neigh"+inputValue[0]+inputValue[1]+inputValue[2]);

        ArrayList<int[]> neighbours = new ArrayList<>();
        for(int w_index = 0; w_index < this.w; w_index++){
            for(int h_index = 0; h_index < this.h; h_index++){
                if (color_diff.cal_color(inputValue, inputValues[w_index][h_index]) <= epsilon) {
                    neighbours.add(new int[]{w_index, h_index});
                }
            }
        }
        return neighbours;
    }

    private ArrayList<Pair<int[], int[]>> mergeRightToLeftCollection(ArrayList<Pair<int[], int[]>> neighbours1, ArrayList<Pair<int[], int[]>> neighbours2) {
        for (int i = 0; i < neighbours2.size(); i++) {
            Pair<int[], int[]> tempPt = neighbours2.get(i);
            if (!neighbours1.contains(tempPt)) {
                neighbours1.add(tempPt);
            }
        }
        return neighbours1;
    }
}
