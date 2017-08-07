package com.android.citic.transmanager.trans.helper.translog;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.FileUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.global.GlobalCfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TransLog implements Serializable {
	private static String TranLogPath = "translog.dat";
	private static String ScriptPath = "script.dat";
	private static String ReversalPath = "reversal.dat";

	private List<TransLogData> transLogData = new ArrayList<TransLogData>();
	private static TransLog tranLog;

	private TransLog() {
	}

	public static TransLog getInstance() {
		if (tranLog == null) {
			String filepath = GlobalCfg.getROOT_FILE_PATH() + TranLogPath;
			try {
				tranLog = ((TransLog) FileUtil.file2Object(filepath));
			} catch (FileNotFoundException e) {
				Logger.debug("translog file not found");
				tranLog = null;
			} catch (IOException e) {
				Logger.debug("translog IOException");
				tranLog = null;
			} catch (ClassNotFoundException e) {
				Logger.debug("translog ClassNotFound");
				tranLog = null;
			}if (tranLog == null) {
				tranLog = new TransLog();
			}
		}
		return tranLog;
	}

	public List<TransLogData> getData() {
		return transLogData;
	}

	public int getSize() {
		return transLogData.size();
	}

	public TransLogData get(int position) {
		if (!(position > getSize()))
			return transLogData.get(position);
		return null;
	}

	public void clearAll() {
		// 清除交易记录的二进制文件
		transLogData.clear();
		String FullName = GlobalCfg.getROOT_FILE_PATH() + TranLogPath;
		File file = new File(FullName);
		if (file.exists())
			file.delete();
	}

	/**
	 * 获取上一条交易记录
	 */
	public TransLogData getLastTransLog() {
		if (getSize() >= 1)
			return transLogData.get(getSize() - 1);
		return null;
	}

	/**
	 * 保存交易记录
	 * @return
	 */
	public boolean saveLog(TransLogData data) {
		// tranLog.setLogs(transLogData);
		// 先暂设置交易记录数量超过最大交易笔数自动清除。
		if (transLogData.size() > Integer.parseInt(GlobalCfg.getInstance().getMaxTrans())) {
			File file = new File(GlobalCfg.getROOT_FILE_PATH() + TranLogPath);
			if (file.exists())
				file.delete();
			transLogData.clear();
		}
		transLogData.add(data);
		Logger.debug("transLogData size " + transLogData.size());
		try {
			FileUtil.object2File(tranLog, GlobalCfg.getROOT_FILE_PATH()+ TranLogPath);
		} catch (FileNotFoundException e) {
			Logger.debug("save translog file not found");
			return false;
		} catch (IOException e) {
			Logger.debug("save translog IOException");
			return false;
		}
		return true;
	}

	/**
	 * 更新交易记录
	 * @param logIndex 交易记录索引
	 * @param newData 更新后的数据
	 * @return 更新结果
	 */
	public boolean updateTransLog(int logIndex, TransLogData newData) {
		if (getSize() > 0) {
			transLogData.set(transLogData.indexOf(transLogData.get(logIndex)), newData);
			return true;
		}
		return false;
	}

	public int getCurrentIndex(TransLogData data){
		int current = -1 ;
		for (int i = 0 ; i < transLogData.size() ; i++){
			if(transLogData.get(i).getTraceNo().equals(data.getTraceNo())){
				current = i ;
			}
		}
		return current ;
	}

	/**
	 * 根据索引获取交易记录
	 * @param logIndex 交易记录索引
	 * @return 交易对象
	 */
	public TransLogData searchTransLogByIndex(int logIndex) {
		if (getSize() > 0 && getSize() - 1 >= logIndex)
			return transLogData.get(logIndex);
		return null;
	}

	/**
	 * 根据流水号获取交易记录
	 * @param TraceNo 交易流水号
	 * @return 交易记录
	 */
	public TransLogData searchTransLogByTraceNo(String TraceNo) {
		if (getSize() > 0)
			for (int i = 0; i < getSize(); i++) {
				if (!StringUtil.isNullWithTrim(transLogData.get(i).getTraceNo()))
					if (transLogData.get(i).getTraceNo().equals("" + TraceNo))
						return transLogData.get(i);
			}
		return null;
	}

	//edit by liyo
	public int searchPosByTraceNo(String TraceNo) {
		if (getSize() > 0)
			for (int i = 0; i < getSize(); i++) {
				if (!StringUtil.isNullWithTrim(transLogData.get(i).getTraceNo()))
					if (transLogData.get(i).getTraceNo().equals("" + TraceNo))
						return i;
			}
		return -1;
	}
	/**
	 * 保存脚本结果
	 * @return
	 */
	public static boolean saveScriptResult(TransLogData data) {
		try {
			FileUtil.object2File(data, GlobalCfg.getROOT_FILE_PATH()+ ScriptPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 保存冲正信息
	 * @return
	 */
	public static boolean saveReversal(TransLogData data ) {
		try {
			FileUtil.object2File(data, GlobalCfg.getROOT_FILE_PATH()+ ReversalPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 获取冲正信息
	 * @return
	 */
	public static TransLogData getReversal() {
		try {
			return (TransLogData) FileUtil.file2Object(GlobalCfg.getROOT_FILE_PATH() + ReversalPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取脚本信息
	 * @return
	 */
	public static TransLogData getScriptResult() {
		try {
			return (TransLogData) FileUtil.file2Object(GlobalCfg.getROOT_FILE_PATH()+ ScriptPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 清除冲正
	 * @return
	 */
	public static boolean clearReveral() {
		File file = new File(GlobalCfg.getROOT_FILE_PATH()+ ReversalPath);
		if (file.exists() && file.isFile()) {
			file.delete();
			return false;
		} else
			return true;
	}

	/**
	 * 清除脚本执行结果
	 * @return
	 */
	public static boolean clearScriptResult() {
		File file = new File(GlobalCfg.getROOT_FILE_PATH()+ ScriptPath);
		if (file.exists() && file.isFile()) {
			file.delete();
			return false;
		} else
			return true;
	}
}