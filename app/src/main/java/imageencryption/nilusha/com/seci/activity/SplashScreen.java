package imageencryption.nilusha.com.seci.activity;

import android.content.Intent;

import com.daimajia.androidanimations.library.Techniques;

import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

import imageencryption.nilusha.com.seci.R;
import imageencryption.nilusha.com.seci.Sign.SignIn;


public class SplashScreen extends AwesomeSplash {
    //private SharedPreferences mSharedPreferences;
    @Override
    public void initSplash(ConfigSplash configSplash) {

        /* you don't have to override every property */

        //Customize Circular Reveal
        configSplash.setBackgroundColor(R.color.strokeColor); //any color you want form colors.xml
        configSplash.setAnimCircularRevealDuration(1000); //int ms
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        //Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        //Customize Logo
        configSplash.setLogoSplash(R.drawable.seciimage); //or any other drawable
        configSplash.setAnimLogoSplashDuration(1000); //int ms
        configSplash.setAnimLogoSplashTechnique(Techniques.BounceIn); //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)


        //Customize Path
        //configSplash.setPathSplash(Constants.DROID_LOGO); //set path String
        configSplash.setOriginalHeight(400); //in relation to your svg (path) resource
        configSplash.setOriginalWidth(400); //in relation to your svg (path) resource
        configSplash.setAnimPathStrokeDrawingDuration(3000);
        configSplash.setPathSplashStrokeSize(3); //I advise value be <5
        configSplash.setPathSplashStrokeColor(R.color.accent); //any color you want form colors.xml
        configSplash.setAnimPathFillingDuration(3000);
        configSplash.setPathSplashFillColor(R.color.Wheat); //path object filling color


        //Customize Title
        configSplash.setTitleSplash("");
        configSplash.setTitleTextColor(R.color.WhiteSmoke);
        configSplash.setTitleTextSize(60f); //float value
        configSplash.setAnimTitleDuration(2000);
        configSplash.setAnimTitleTechnique(Techniques.Landing);
        configSplash.setTitleFont("fonts/diti_sweet.ttf"); //provide string to your font located in assets/fonts/

    }

    @Override
    public void animationsFinished() {


        Intent intent2 = new Intent(SplashScreen.this, SignIn.class);
        startActivity(intent2);
//        mSharedPreferences = getSharedPreferences(SignIn.AUTH, Context.MODE_PRIVATE);
//        mSharedPreferences.edit().clear().commit();



    }


}

