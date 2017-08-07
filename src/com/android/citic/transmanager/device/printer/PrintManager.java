package com.android.citic.transmanager.device.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import com.android.citic.R;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.AssetsUtil;
import com.android.citic.lib.utils.DateUtil;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.finace.FinanceTrans;
import com.android.citic.transmanager.trans.helper.translog.TransLog;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
import com.pos.device.icc.IccReader;
import com.pos.device.printer.PrintCanvas;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;
import com.pos.device.printer.PrinterCallback;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

/**
 * 打印管理类
 */
public class PrintManager {

	private static PrintManager mInstance ;

	private static GlobalCfg cfg ;

	private PrintManager(){}

	private static Context mContext ;

	public static PrintManager getmInstance(Context c){
		mContext = c ;
		if(null == mInstance){
			mInstance = new PrintManager();
		}
		cfg = GlobalCfg.getInstance();
		return mInstance ;
	}

	private Printer printer = null ;
	private PrintTask printTask = null ;
	private boolean printFlag = false ;
	public int start(final TransLogData data, final boolean isRePrint){
		this.printTask = new PrintTask();
		this.printFlag = true;
		Trans.printFlag = true ;
		boolean isICC = data.isICC();
		boolean isNFC = data.isNFC();
		boolean isBarcode = data.isBarcode();
		int ret = -1;

		Logger.debug("start print");
		if (TransLog.getInstance().getSize() == 0) {
			ret = Tcode.T_print_no_log_err;
		}else {
			printer = Printer.getInstance() ;
			if (printer == null) {
				ret = Tcode.T_sdk_err ;
			}else{
				int num = cfg.getPrinterTickNumber() ;//中信银行只打印一联
				Logger.debug("PrintManager>>start>>PrinterTickNumber="+num);
				for (int i = 0; i < num; ) {
					Logger.debug("PrintManager>>start>>for>>i="+i);
					PrintCanvas canvas = new PrintCanvas() ;
					Paint paint = new Paint() ;
					setFontStyle(paint , 2 , false);
					canvas.drawText(PrintRes.EN.WANNING , paint);
					printLine(paint , canvas);
					setFontStyle(paint , 1 , true);
					Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.citic_logo);
					canvas.drawBitmap(image , paint);
					if(!image.isRecycled()){
						image.recycle();
					}
					printLine(paint , canvas);
					setFontStyle(paint , 2 , false);
					if (i == 0) {
						canvas.drawText(PrintRes.EN.MERCHANT_COPY, paint);
					}else if (i == 1){
						canvas.drawText(PrintRes.EN.CARDHOLDER_COPY , paint);
					}else{
						canvas.drawText(PrintRes.EN.BANK_COPY , paint);
					}
					printLine(paint , canvas);

					Logger.debug("PrintManager>>start>>point1");
					setFontStyle(paint , 2 , false);
					canvas.drawText(PrintRes.EN.MERCHANT_NAME+"\n"+cfg.getMerchName() , paint);
					canvas.drawText(PrintRes.EN.MERCHANT_ID+"\n"+cfg.getMerchID() , paint);
					canvas.drawText(PrintRes.EN.TERNIMAL_ID+"\n"+cfg.getTermID() , paint);
					String operNo = data.getOprNo() < 10 ? "0" + data.getOprNo() : data.getOprNo()+"";
					canvas.drawText(PrintRes.EN.OPERATOR_NO+"    "+operNo, paint);
					printLine(paint , canvas);
					setFontStyle(paint , 2 , false);
					canvas.drawText(PrintRes.EN.ISSUER, paint);
					canvas.drawText(PrintRes.EN.ACQUIRER, paint);
//					if(data.getField44()!=null && !data.getEName().equals(TransEN.ENQUIRY)){
//						if(data.getField44().length() > 11){
//							String[] str = ISOUtil.subStrByLen(data.getField44(), 11);
//							canvas.drawText(mContext.getString(R.string.recept_issuer)+":" + getBankInfo(str[0]) , paint);
//							canvas.drawText(mContext.getString(R.string.recept_acquirer)+":" + getBankInfo(str[1]) , paint);
//						}
//					}
					if(isBarcode)
						canvas.drawText("BARCODE NO:", paint);
					else
						canvas.drawText(PrintRes.EN.CARD_NO, paint);
					Logger.debug("PrintManager>>start>>point2");
					setFontStyle(paint , 3 , true);
					if (isICC){
						canvas.drawText("     "+ StringUtil.getSecurityNum(data.getPan(), 6, 4) + " I" , paint);
					}else if(isNFC){
						canvas.drawText("     "+ StringUtil.getSecurityNum(data.getPan(), 6, 4) + " C" , paint);
					}else if(isBarcode){
						canvas.drawText("     "+ data.getCardFullNo() + " B" , paint);
					}else{
						canvas.drawText("     "+ StringUtil.getSecurityNum(data.getPan(), 6, 4) + " S" , paint);
					}
					Logger.debug("PrintManager>>start>>point3");
					setFontStyle(paint , 2 , false);
					canvas.drawText(PrintRes.EN.TRANS_TYPE , paint);
					setFontStyle(paint , 3 , true);
					//canvas.drawText(formatTranstype(data.getEName()) , paint);
					canvas.drawText(data.getEName(), paint);
					setFontStyle(paint , 2 , false);
					if (!StringUtil.isNullWithTrim(data.getExpDate())){
						canvas.drawText(PrintRes.EN.CARD_EXPDATE+"       " + data.getExpDate() , paint);
					}
					printLine(paint , canvas);
					Logger.debug("PrintManager>>start>>point4");
					setFontStyle(paint , 2 , false);
					if(!StringUtil.isNullWithTrim(data.getBatchNo())){
						canvas.drawText(PrintRes.EN.BATCH_NO + data.getBatchNo(), paint);
					}
					if(!StringUtil.isNullWithTrim(data.getTraceNo())){
						canvas.drawText(PrintRes.EN.VOUCHER_NO+data.getTraceNo(), paint);
					}
					if(!StringUtil.isNullWithTrim(data.getAuthCode())){
						canvas.drawText(PrintRes.EN.AUTH_NO+data.getAuthCode() , paint);
					}
					setFontStyle(paint , 2 , false);
					if(!StringUtil.isNullWithTrim(data.getLocalDate()) && !StringUtil.isNullWithTrim(data.getLocalTime())){
						String timeStr = StringUtil.StringPattern(data.getLocalDate() + data.getLocalTime(), "yyyyMMddHHmmss", "yyyy/MM/dd  HH:mm:ss");
						canvas.drawText(PrintRes.EN.DATE_TIME+"\n          " + timeStr, paint);
					}
					if(!StringUtil.isNullWithTrim(data.getRRN())){
						canvas.drawText(PrintRes.EN.REF_NO+ data.getRRN(), paint);
					}
					canvas.drawText(PrintRes.EN.AMOUNT, paint);
					setFontStyle(paint , 3 , true);
					canvas.drawText("           "+PrintRes.EN.RMB+"     "+ getStrAmount(data.getAmount()) + "" , paint);
					printLine(paint , canvas);
					Logger.debug("PrintManager>>start>>point3");
					setFontStyle(paint , 1 , false);
					if(!StringUtil.isNullWithTrim(data.getRefence())){
						canvas.drawText(PrintRes.EN.REFERENCE +"\n" + data.getRefence() , paint);
					}
					//追加ICC数据
					if ( (isICC || isNFC) && !data.getEName().equals(Trans.Type.EC_ENQUIRY) && !data.getEName().equals(Trans.Type.VOID) ) {
						printAppendICCData(data.getICCData() , canvas , paint);
					}
					if (isRePrint) {
						setFontStyle(paint , 3 , true);
						canvas.drawText(PrintRes.EN.REPRINT , paint);
					}
					if (i != 1 ) {
						setFontStyle(paint , 3 , true);
						canvas.drawText("      "+PrintRes.EN.CARDHOLDER_SIGN+"\n\n\n" , paint);
						printLine(paint , canvas);
						setFontStyle(paint , 1 , false);
						canvas.drawText(PrintRes.EN.AGREE_TRANS+"\n" , paint);
					}
					ret = printData(canvas);
					if (ret == Printer.PRINTER_OK) {
						if (i + 1 < cfg.getPrinterTickNumber()) { // 还有联要打印,延时1秒撕纸
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else{
							Trans.printFlag = false; // 打印完了
						}
						i++;
					} else {
//						ret = checkPrinterStatus();
//						if (ret != Printer.PRINTER_OK)
//							break;
					}
				}
			}
		}

		return ret;
	}

	public int startPrintSettle(final TransLogData data){
		this.printTask = new PrintTask();
		this.printFlag = true;
		Trans.printFlag = true ;
		int ret ;

		printer = Printer.getInstance() ;
		if(printer == null){
			return Tcode.T_sdk_err ;
		}

		PrintCanvas canvas = new PrintCanvas() ;
		Paint paint = new Paint() ;

		//结算单
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.EN.WANNING , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 1 , true);
		Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.citic_logo);
		canvas.drawBitmap(image , paint);
		if(!image.isRecycled()){
			image.recycle();
		}
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.EN.SETTLE_SUMMARY, paint);
		printLine(paint , canvas);
		setFontStyle(paint , 2 , false);
		canvas.drawText(PrintRes.EN.MERCHANT_NAME+"\n"+cfg.getMerchName() , paint);
		canvas.drawText(PrintRes.EN.MERCHANT_ID+"\n"+cfg.getMerchID() , paint);
		canvas.drawText(PrintRes.EN.TERNIMAL_ID+"\n"+cfg.getTermID() , paint);
		String operNo = data.getOprNo() < 10 ? "0" + data.getOprNo() : data.getOprNo()+"";
		canvas.drawText(PrintRes.EN.OPERATOR_NO+"    "+operNo, paint);
		if(!StringUtil.isNullWithTrim(data.getBatchNo())){
			canvas.drawText(PrintRes.EN.BATCH_NO + data.getBatchNo(), paint);
		}
		if(!StringUtil.isNullWithTrim(data.getLocalDate()) && !StringUtil.isNullWithTrim(data.getLocalTime())){
			String timeStr = StringUtil.StringPattern(data.getLocalDate() + data.getLocalTime(), "yyyyMMddHHmmss", "yyyy/MM/dd  HH:mm:ss");
			canvas.drawText(PrintRes.EN.DATE_TIME+"\n          " + timeStr, paint);
		}
		printLine(paint , canvas);
		canvas.drawText(PrintRes.EN.SETTLE_LIST , paint);
		printLine(paint , canvas);
		canvas.drawText(PrintRes.EN.SETTLE_INNER_CARD , paint);
		List<TransLogData> list = TransLog.getInstance().getData();
		int saleAmount = 0 ;
		int saleSum = 0 ;
		int quickAmount = 0 ;
		int quickSum = 0 ;
		int voidAmount = 0 ;
		int voidSum = 0 ;
		for (int i = 0 ; i < list.size() ; i++ ){
			TransLogData tld = list.get(i) ;
			if(tld.getEName().equals(Trans.Type.SALE)){
				saleAmount += tld.getAmount() ;
				saleSum ++ ;
			}if(tld.getEName().equals(Trans.Type.QUICKPASS)){
				if(tld.getAAC() == FinanceTrans.AAC_ARQC){
					saleAmount += tld.getAmount() ;
					saleSum ++ ;
				}else{
					quickAmount += tld.getAmount() ;
					quickSum ++ ;
				}
			}if(tld.getEName().equals(Trans.Type.VOID)){
				voidAmount += tld.getAmount() ;
				voidSum ++ ;
			}
		}

		if(saleSum != 0){
			canvas.drawText(formatTranstype(Trans.Type.SALE)+"           "+saleSum+"               "+getStrAmount(saleAmount) , paint);
		}if(quickSum != 0){
			canvas.drawText("SALE(EC)"+"               "+quickSum+"               "+getStrAmount(quickAmount) , paint);
		}if(voidSum != 0){
			canvas.drawText(formatTranstype(Trans.Type.VOID)+"           "+voidSum+"               "+getStrAmount(voidAmount) , paint);
		}

		printLine(paint , canvas);
		canvas.drawText(PrintRes.EN.SETTLE_OUTER_CARD , paint);

		canvas.drawText("\n\n\n\n\n" , paint);

		//明细单
		canvas.drawText(PrintRes.EN.WANNING , paint);
		printLine(paint , canvas);
		setFontStyle(paint , 1 , true);
		Bitmap image1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.citic_logo);
		canvas.drawBitmap(image1 , paint);
		if(!image1.isRecycled()){
			image1.recycle();
		}
		setFontStyle(paint , 2 , false);
		printLine(paint , canvas);
		canvas.drawText(PrintRes.EN.SETTLE_DETAILS, paint);
		printLine(paint , canvas);

		canvas.drawText(PrintRes.EN.SETTLE_DETAILS_LIST_EN , paint);
		setFontStyle(paint , 2 , false);
		printLine(paint , canvas);

		//添加明细
		List<TransLogData> list1 = TransLog.getInstance().getData();
		for (int i = 0 ; i < list1.size() ; i++ ){
			TransLogData tld = list1.get(i) ;
			if(tld.getEName().equals(Trans.Type.SALE) || tld.getEName().equals(Trans.Type.QUICKPASS) || tld.getEName().equals(Trans.Type.VOID)){
				canvas.drawText(tld.getTraceNo()+"     "+
						formatDetailsType(tld)+"    "+
						formatDetailsAuth(tld)+"    "+
						getStrAmount(tld.getAmount())+"   "+
						tld.getPan() , paint);
			}
		}

		ret = printData(canvas);
		if (ret == Printer.PRINTER_OK) {
			Trans.printFlag = false; // 打印完了
		}

		return ret ;
	}

	/**
	 * 打印
	 * @param pCanvas
	 * @return
     */
	private int printData(PrintCanvas pCanvas) {
		printTask.setPrintCanvas(pCanvas);
//		int ret = checkPrinterStatus();
		int ret = printer.getStatus();
		Logger.debug("printer.getStatus="+ret);
		if (ret != Printer.PRINTER_OK)
			return ret;
		printFlag = true;
		printer.startPrint(printTask, new PrinterCallback() {
			@Override
			public void onResult(int i, PrintTask printTask) {
				Logger.debug("PrinterCallback i = " + i);
				printFlag = false;
			}
		});
//		long t0 = System.currentTimeMillis();
//		while (true) {
//			if (System.currentTimeMillis() - t0 > 30000) {
//				return -1;
//			}if (printFlag) { // 还没回调
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				continue;
//			}
//			ret = printer.getStatus();
//			ret = 0 ;
//			break;
//		}
		return ret;
	}

	/**
	 * 检查打印机状态
	 * @return
     */
	private int checkPrinterStatus() {
		long t0 = System.currentTimeMillis();
		int ret ;
		while (true) {
			if (System.currentTimeMillis() - t0 > 30000) {
				ret = -1 ;
				break;
			}
			ret = printer.getStatus();
			Logger.debug("printer.getStatus() ret = "+ret);
			if (ret == Printer.PRINTER_OK) {
				Logger.debug("printer.getStatus()=Printer.PRINTER_OK");
				Logger.debug("打印机状态正常");
				break;
			}else if (ret == -3) {
				Logger.debug("printer.getStatus()=Printer.PRINTER_STATUS_PAPER_LACK");
				Logger.debug("提示用户装纸...");
				break;
			} else if (ret == Printer.PRINTER_STATUS_BUSY) {
				Logger.debug("printer.getStatus()=Printer.PRINTER_STATUS_BUSY");
				Logger.debug("打印机忙");
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		if (ret != Printer.PRINTER_OK)
			Trans.printFlag = false;
		return ret;
	}

	/** =======================私有处理方法=====================**/

	private String formatTranstype(String type){
		int index = 0 ;
		for (int i = 0 ; i < PrintRes.TRANSEN.length ; i++){
			if(PrintRes.TRANSEN[i].equals(type)){
				index = i ;
			}
		}
		return PrintRes.TRANSEN[index]+"("+type+")";
	}

	private Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof NinePatchDrawable) {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
		} else {
			return null;
		}
	}

	private String formatDetailsType(TransLogData data){
		if(data.isICC()){
			return "I" ;
		}else if(data.isNFC()){
			return "C" ;
		}else {
			return "S" ;
		}
	}

	private String formatDetailsAuth(TransLogData data){
		if(data.getAuthCode() == null){
			return "000000" ;
		}else {
			return data.getAuthCode() ;
		}
	}

	private Bitmap compress(Context c , int rid){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		BitmapFactory.decodeResource(c.getResources(), rid).compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] bytes = stream.toByteArray();
		return BitmapFactory.decodeByteArray(bytes , 0 , bytes.length);
	}

	private String getBankInfo(String bankcode) {
		Properties pro = AssetsUtil.lodeConfig(mContext, "props/bankcodelist.properties");
		try {
			return new String(pro.getProperty(
					ISOUtil.padright(bankcode, 8, '0')).getBytes("ISO-8859-1"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 设置打印字体样式
	 * @param paint 画笔
	 * @param size 字体大小 1---small , 2---middle , 3---large
	 * @param isBold 是否加粗
	 * @author zq
	 */
	private void setFontStyle(Paint paint , int size , boolean isBold){
		if(isBold)
			paint.setTypeface(Typeface.DEFAULT_BOLD);
		else
			paint.setTypeface(Typeface.DEFAULT) ;
		switch (size) {
			case 0 : break;
			case 1 : paint.setTextSize(16F) ;break;
			case 2 : paint.setTextSize(22F) ;break;
			case 3 : paint.setTextSize(30F) ;break;
			default:break;
		}
	}

	/**
	 * 在画布上画出一条线
	 * @param paint
	 * @param canvas
	 */
	private void printLine(Paint paint , PrintCanvas canvas){
		String line = "----------------------------------------------------------------";
		setFontStyle(paint , 1 , true);
		canvas.drawText(line , paint);
	}

	public static String getStrAmount(long Amount) {
		double f1 = Double.valueOf(Amount + "");
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(f1 / 100);
	}

	private void printAppendICCData(byte[] ICCData , PrintCanvas canvas , Paint paint){
		byte[] temp = new byte[256];
		int len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x4F, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("AID: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x50, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("LABLE: "+ ISOUtil.hex2AsciiStr(ISOUtil.byte2hex(temp, 0, len)) , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9F26, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TC: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x5F34,temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("PanSN: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x95, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TVR: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9B, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TSI: "+ ISOUtil.byte2hex(temp, 0, len) , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9F36, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("ATC: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9F33, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("TermCap: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9F09, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("AppVer: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9F34, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("CVM: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9F10, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("IAD: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x82, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("AIP: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
		len = TLVUtil.get_tlv_data(ICCData, ICCData.length, 0x9F1E, temp, false);
		if (!StringUtil.isNullWithTrim(ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len)))){
			canvas.drawText("IFD: "+ ISOUtil.byte2hex(temp, 0, len) + "" , paint);
		}
	}
}
