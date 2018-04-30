package activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.graphics.Color;
import com.example.smartparking.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
public class KeyActivity extends Activity{
    QRCodeWriter writer ;
    ImageView tnsd_iv_qr ;
    String data;
    BitMatrix bitMatrix;
    int width = 800;
    int height = 800;
    Bitmap bmp;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);
        tnsd_iv_qr = (ImageView) findViewById(R.id.tnsd_iv_qr);
        writer=new QRCodeWriter();
        myMethod();
    }
    public void myMethod() {
        data = "/http/.....";
        try {
            bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
        } catch (WriterException e1) {
            e1.printStackTrace();
        }
        bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if ( bitMatrix.get(x, y) == true ) {
                    bmp.setPixel(x, y, Color.BLACK);
                } else
                    bmp.setPixel(x, y, Color.WHITE);
            }
        }
        tnsd_iv_qr.setImageBitmap(bmp);
    }

}
