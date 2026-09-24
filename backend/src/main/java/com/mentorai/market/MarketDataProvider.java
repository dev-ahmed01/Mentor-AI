package com.mentorai.market;

import java.util.List;
import com.mentorai.market.MarketModels.RawJob;

/** Replaceable trusted adapter. Neither URLs nor provider selection come from API users. */
public interface MarketDataProvider {
    List<RawJob> fetch() throws Exception;
}
