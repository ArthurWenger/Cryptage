package com.example.arthur.cryptage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main);
	}

	public void showVigenere( View view ) {
		Intent vigenereIntent = new Intent( this, VigenereActivity.class );
		startActivity(vigenereIntent);
	}

	public void showCesar( View view ) {
		Intent cesarIntent = new Intent( this, CesarActivity.class );
		startActivity(cesarIntent);
	}

	public void showAtbash( View view ) {
		Intent atbashIntent = new Intent( this, AtbashActivity.class);
		startActivity(atbashIntent);
	}

	public void showPolybe( View view ) {
		Intent polybeIntent = new Intent( this, PolybeActivity.class);
		startActivity(polybeIntent);
	}

	public void showPlayfair( View view ) {
		Intent playfairIntent = new Intent( this, PlayfairActivity.class);
		startActivity(playfairIntent);
	}

	public void showHill( View view ) {
		Intent hillIntent = new Intent( this, HillActivity.class);
		startActivity(hillIntent);
	}

	public void showTRect( View view ) {
		Intent trectIntent = new Intent( this, TRectActivity.class);
		startActivity(trectIntent);
	}

	public void showDelastelle( View view ) {
		Intent delastelleIntent = new Intent( this, DelastelleActivity.class);
		startActivity(delastelleIntent);
	}
	public void showDES( View view ) {
		Intent desIntent = new Intent( this, DESActivity.class);
		startActivity(desIntent);
	}

	public void showSDES(View view) {
		Intent sdesIntent = new Intent( this, SDESActivity.class);
		startActivity(sdesIntent);
	}

	public void exitApp(View view) {
		finish();
		System.exit( 0 );
	}
}
