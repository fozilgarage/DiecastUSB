package Utilities;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import databases.Car;
import databases.DataBaseManager;

/**
 * Created by eduardo on 25/10/2017.
 */

public class FileUtils {

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static void deleteFilesFromDirectory(final File dir) {
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            Log.e("FileUtils", e.getMessage());
            return null;
        }
    }

    public static void exportToExcel(final List<Car> carList, final File directory,
                                     final String fileName, final DataBaseManager dataBaseManager) {

        try {
            //create directory if not exist
            if (!directory.isDirectory()) {
                directory.mkdirs();
            }

            //file path
            File file = new File(directory, fileName);

            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = (XSSFSheet) wb.createSheet("Autos");

            XSSFRow rowHeader = sheet.createRow(0);
            rowHeader.createCell(0).setCellValue("No");
            rowHeader.createCell(1).setCellValue("Nombre");
            rowHeader.createCell(2).setCellValue("Fabricante");
            rowHeader.createCell(3).setCellValue("Serie");
            rowHeader.createCell(4).setCellValue("Subserie");
            rowHeader.createCell(5).setCellValue("Favorito");
            rowHeader.createCell(6).setCellValue("Cantidad");
            rowHeader.createCell(7).setCellValue("Precio");
            rowHeader.createCell(8).setCellValue("Fecha de Compra");
            rowHeader.createCell(9).setCellValue("Información Extra");
            rowHeader.createCell(10).setCellValue("Imagen");
            rowHeader.createCell(11).setCellValue("Fecha de agregado");

            int rowNum = 1;

            for (Car car : carList) {

                XSSFRow rowCar = sheet.createRow(rowNum);
                rowCar.createCell(0).setCellValue(rowNum);
                rowCar.createCell(1).setCellValue(car.getName());
                rowCar.createCell(2).setCellValue(car.getBrand().getName());
                String serie = "";
                String subserie = "";
                if (car.getSerie() != null) {
                    serie = car.getSerie().getName();
                    if (car.getSerie().getParent() != null && car.getSerie().getParent().getId() > 0) {
                        subserie = serie;
                        serie = dataBaseManager.getSerieById(car.getSerie().getParent().getId()).getName();
                    }
                }
                rowCar.createCell(3).setCellValue(serie);
                rowCar.createCell(4).setCellValue(subserie);
                rowCar.createCell(5).setCellValue((car.isFavorite() ? "SI" : "NO"));
                rowCar.createCell(6).setCellValue(car.getCount() + "");
                rowCar.createCell(7).setCellValue(car.getPrice());
                rowCar.createCell(8).setCellValue(car.getPurchaseDate());
                rowCar.createCell(9).setCellValue(car.getExtra());
                rowCar.createCell(10).setCellValue(car.getImage());
                rowCar.createCell(11).setCellValue(getDateExcelFormat(car.getCreatedAt()));
                rowNum++;
            }

            FileOutputStream fileOut = new FileOutputStream(directory.getAbsolutePath().toString() + "/" + fileName);

            wb.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private static String getDateExcelFormat(String dateDB) {
        try {
            if (dateDB.length() == 10) dateDB += " 00:00:00";
            if (dateDB.contains("/")) {
                return dateDB;
            } else {
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date createdAt = sdf1.parse(dateDB);
                return sdf2.format(createdAt);
            }

        } catch (Exception e) {
            Log.e("FileUtils", e.getMessage());
        }
        return "";
    }

}