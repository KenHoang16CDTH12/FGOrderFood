package it.hueic.kenhoang.orderfoods_app.model;

import java.util.List;

/**
 * Created by kenhoang on 09/02/2018.
 */

public class MyReponse {
    public long multicast_id;
    public int sucess;
    public int failure;
    public int canonical_ids;
    public List<Result> results;
}
