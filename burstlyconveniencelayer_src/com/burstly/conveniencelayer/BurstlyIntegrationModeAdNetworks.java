package com.burstly.conveniencelayer;

/**
 * Created By: David Hopkins
 * Email: dhopkins@Burstly.com
 * Date: 8/24/12
 *
 * Enum holding all Ad Network data
 */
public enum BurstlyIntegrationModeAdNetworks {
    HOUSE("0959195979157244033", "0656195979157244033", "House Ad"),
    MILLENIAL("0952195079157254033", "0052195179157254033", "Millenial"),
    ADMOB("0655195179157254033", "0755195179157254033", "AdMob"),
    GREYSTRIPE("0955195179157254033", "0555195079157254033", "Greystripe"),
    INMOBI("0755195079157254033", "0855195079157254033", "InMobi"),
    REWARDS_SAMPLE(null, "0954195379157264033", "Rewards Sample");
    //TODO set up banner for rewards sample

    private String bannerZone;
    private String interstitialZone;
    private String adName;
    // This assumes that all zones are from the same pub
    public final String appId = "Js_mugok3kCBg8ABoJj_Cg";

    BurstlyIntegrationModeAdNetworks(String bannerZone, String interstitialZone, String adName) {
        this.bannerZone = bannerZone;
        this.interstitialZone = interstitialZone;
        this.adName = adName;
    }

    //getters
    public String getBannerZone() {
        return bannerZone;
    }
    public String getAdName() {
        return adName;
    }
    public String getInterstitialZone() {
        return interstitialZone;
    }

}
