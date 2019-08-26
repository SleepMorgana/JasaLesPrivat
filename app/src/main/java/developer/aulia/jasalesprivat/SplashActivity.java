package developer.aulia.jasalesprivat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;



import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class SplashActivity extends Activity {
    //Gunakan Splash Screen untuk melakukan pemeriksaan pada perangkat, seperti jika perangkat tersambung ke
    //internet
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //Saat ini sistem tidak melakukan pemeriksaan
        new Thread() {
            @Override
            public void run() {
                try {
                    super.run();

                    //inisialisasi splash screen
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "There were some issues loading the application, please try again later",
                            Toast.LENGTH_SHORT).show();
                } finally {
                    Intent intent = new Intent(getApplicationContext(),SignInSignUp.class);
                    startActivity(intent);
                    finish();
                }
            }
        }.start();
    }
}
