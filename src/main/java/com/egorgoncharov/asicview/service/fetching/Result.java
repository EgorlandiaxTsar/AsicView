package com.egorgoncharov.asicview.service.fetching;

import com.egorgoncharov.asicview.service.fetching.data.AsicEntity;
import com.egorgoncharov.asicview.service.parsing.Response;

public class Result {
    private final AsicEntity asic;
    private final Response response;

    public Result(AsicEntity asic, Response response) {
        this.asic = asic;
        this.response = response;
    }

    public AsicEntity getAsic() {
        return asic;
    }

    public Response getResponse() {
        return response;
    }
}
