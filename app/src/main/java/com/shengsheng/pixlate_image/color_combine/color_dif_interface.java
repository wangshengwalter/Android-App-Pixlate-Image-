package com.shengsheng.pixlate_image.color_combine;


public interface color_dif_interface<Pair> {

    public double cal_color(Pair val1, Pair val2) throws color_combine_exception;

    public double cal_color_method_1(Pair val1, Pair val2) throws color_combine_exception;

    public double cal_color_method_2(Pair val1, Pair val2) throws color_combine_exception;

}
