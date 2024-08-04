package com.shengsheng.pixlate_image;

import android.graphics.Point;

import java.util.Vector;

public class DBSCAN {

    private static final String TAG = "Mainactivity->Pixlater->DBSCAN";


//    private double euclidean_distance(Point a, Point b) {
//        return sqrt(pow((a.x - b.x), 2) + pow((a.y - b.y), 2));
//    }
//
//    // DBSCAN function
//    void dbscan(Vector<Point> dataset, double eps, int min_pts) {
//        int num_points = dataset.size();
//        int cluster_id = 0;
//        Vector<Integer> cluster_membership = new Vector<>(num_points, -1);
//
//        for (int i = 0; i < num_points; i++) {
//            if (cluster_membership.get(i) != -1) {
//                continue; // Point already assigned to a cluster
//            }
//
//            Vector<Integer> neighbors;
//
//            // Find all points within eps distance from current point
//            for (int j = 0; j < num_points; j++) {
//                if (euclidean_distance(dataset.get(i), dataset.get(j)) <= eps) {
//                    neighbors.add(j);
//                }
//            }
//
//            // If the number of neighbors is less than min_pts, mark as noise
//            if (neighbors.size() < min_pts) {
//                cluster_membership.set(i, 0); // Mark as noise
//            } else {
//                cluster_id++;
//                cluster_membership.set(i, cluster_id);
//
//                // Expand the cluster to all reachable points
//                for (int k = 0; k < neighbors.size(); k++) {
//                    int index = neighbors.get(k);
//                    if (cluster_membership.get(index) == 0) {
//                        cluster_membership.set(index, cluster_id);
//                    }
//                    if (cluster_membership.get(index) != -1) {
//                        continue; // Point already assigned to a cluster
//                    }
//                    cluster_membership.set(index, cluster_id);
//
//                    Vector<Integer> sub_neighbors;
//
//                    // Find all points within eps distance from the neighbor
//                    for (int j = 0; j < num_points; j++) {
//                        if (euclidean_distance(dataset.get(index), dataset.get(j)) <= eps) {
//                            sub_neighbors.add(j);
//                        }
//                    }
//
//                    // If the number of sub-neighbors is greater than min_pts, add to neighbors
//                    if (sub_neighbors.size() >= min_pts) {
//                        for (int l = 0; l < sub_neighbors.size(); l++) {
//                            if (std::find(neighbors.begin(), neighbors.end(), sub_neighbors[l]) == neighbors.end()) {
//                                neighbors.push_back(sub_neighbors[l]);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Print the clustering results
//        std::cout << "DBSCAN clustering results:" << std::endl;
//        for (int i = 0; i < num_points; i++) {
//            if (cluster_membership[i] == 0) {
//                std::cout << "Point " << i << " is noise" << std::endl;
//            } else {
//                std::cout << "Point " << i << " is in cluster " << cluster_membership[i] << std::endl;
//            }
//        }
//    }
}
