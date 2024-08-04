package com.shengsheng.pixlate_image.color_combine;

public class color_dif_method<method_num> implements color_dif_interface<float[]> {
    private int method_num = 1;

    public color_dif_method(int i) {
        method_num = i;
    }

    public double cal_color(float[] val1, float[] val2) throws  color_combine_exception{
        switch(method_num){
            case 1:
                return cal_color_method_1(val1, val2);
            case 2:
                return cal_color_method_2(val1, val2);
            default:
                return 0;
        }
    }

    @Override
    public double cal_color_method_1(float[] val1, float[] val2) throws  color_combine_exception{

        float rmean = (float) (( val1[0] + val2[0] ) / 2.0);
        float r = Math.abs(val1[0]  - val2[0]);
        float g = Math.abs(val1[1]  - val2[1]);
        float b = Math.abs(val1[2]  - val2[2]);
        //double ans = Math.sqrt(Math.pow(r,2) + Math.pow(g,2) +Math.pow(b,2));
        double ans = Math.sqrt(Math.pow(r,2) * (2+rmean/256)+ 4*Math.pow(g,2) +(2+(255-r)/256)*Math.pow(b,2));
        //double semblance = (255 - (Math.abs(color1.getRed() - color2.getRed()) * 255 * 0.297 + Math.abs(color1.getGreen() - color2.getGreen()) * 255 * 0.593 + Math.abs(color1.getBlue() - color2.getBlue()) * 255 * 11.0 / 100)) / 255;
        //System.out.println("distance: "+val2.getValue()[0]+ val2.getValue()[1]+val2.getValue()[2]+"     "+val1.getValue()[0]+ val1.getValue()[1]+val1.getValue()[2]+"      "+String.format("%.8f", ans));

        return ans;
    }

    @Override
    public double cal_color_method_2(float[] val1, float[] val2) throws color_combine_exception {
        return 0;
    }

}
