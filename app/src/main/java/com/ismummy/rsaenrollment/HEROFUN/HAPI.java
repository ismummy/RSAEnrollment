package com.ismummy.rsaenrollment.HEROFUN;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import java.io.File;

public class HAPI {
    class DATABASE {

        public static final String TABLE_NAME = "main";
        public static final String DEFAULT_SORT_ORDER = "_id COLLATE LOCALIZED ASC";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_FPDATA = "data";
        private static final String DATABASE_NAME = "fprecord.db";
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
 												   + COLUMN_ID + " INTEGER PRIMARY KEY,"
 												   + COLUMN_NAME + " TEXT,"
 												   + COLUMN_FPDATA + " BLOB"
 												   + ");";
        SQLiteDatabase db;
        
        DATABASE(Context context) {
     	   File file = context.getDatabasePath(DATABASE_NAME);
     	   boolean bFile = file.exists();
     	   db = context.openOrCreateDatabase(DATABASE_NAME, 0, null);
     	   if ( bFile==false ) {
     		   db.execSQL(CREATE_TABLE);
     	   }
        }
        
        public int deleteRow(String where, String[] whereArgs) {
     	   return db.delete(TABLE_NAME, where, whereArgs);
        }       
        public int updateRow(ContentValues values, String where, String[] whereArgs) {
            return db.update(TABLE_NAME, values, where, whereArgs);
        }       
        public int insertRow(ContentValues values) {
            return (int)db.insert(TABLE_NAME, null, values);
        }
        public Cursor queryRow(String[] projection, String selection, String[] selectionArgs, 
 	   			 String sortOrder) {
            if (sortOrder==null) sortOrder = DEFAULT_SORT_ORDER;
            Cursor c = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
     	   return c;
        }
     }
 	//*********************************************************************************************************
	//*********************************************************************************************************
	//*********************************************************************************************************
    //----------------------------The below defines error codes---------------------------------------------//.
    public static final int ERROR_NONE = 0xFFFFFF0;
    public static final int ERROR_ARGUMENTS = 0xFFFFFF1;
    public static final int ERROR_TIMEOUT_OVER = 0xFFFFFF2;
    public static final int ERROR_LOW_QUALITY = 0xFFFFFF4;
    public static final int ERROR_CANT_GENERATE = 0xFFFFFF5;
    public static final int ERROR_NEG_ACCESS = 0xFFFFFF7;
    public static final int ERROR_NEG_FIND = 0xFFFFFF8;
    public static final int ERROR_NEG_DELETE = 0xFFFFFF9;
    public static final int ERROR_INITIALIZE = 0xFFFFF10;
    public static final int ERROR_DEFAULT = 0xFFFFF11;
    public static final int ERROR_OVERFLOW_RECORD = 0xFFFF12;
    public static final int ERROR_NEG_ADDNEW = 0xFFFF14;
    public static final int ERROR_NEG_CLEAR = 0xFFFF15;
    public static final int ERROR_NONE_ACTIVITY = 0xFFFF18;
    public static final int ERROR_NONE_CAPIMAGE = 0xFFFF21;
    public static final int ERROR_NOT_CALIBRATED = 0xFFFF22;
    public static final int ERROR_LICENSE_OVER = 0xFFFF23;
    public static final int ERROR_NONE_DEVICE = 0xFFFF24;
    public static final int ERROR_EMPTY_DADABASE = 0xFFFF25;
    public static final int ERROR_DO_CANCELED= 0xFFFF26;

   //-------------------The below defines message to be sent to Activity----------------------------------//.
    public static final int MSG_PUT_FINGER = 1;
    public static final int MSG_TAKEOFF_FINGER = 2;
    public static final int MSG_RETRY_FINGER = 3;
    public static final int MSG_FINGER_CAPTURED = 4;
    public static final int MSG_CREATED_TEMPLATE = 5;
    public static final int MSG_DBRECORD_START = 20;
    public static final int MSG_DBRECORD_NEXT = 21;
    public static final int MSG_DBRECORD_END = 23;
    public static final int MSG_ON_SEARCHING = 24;
    public static final int MSG_LOW_QUAL_IS_REG = 25;
    public static final int MSG_SHOW_TEXT = 27;

    public static final int CONFIG_FINGER_THESHOLD = 0;
    public static final int CONFIG_ENROLL_THESHOLD = 1;
    public static final int CONFIG_MATCH_THESHOLD = 2;

   //-----------------The below defines object variables for CALLBACK of Activity-------------------------//.
    private Handler m_fHandler = null;
    
    //-----------------The below defines object variables for access with DATABASE-------------------------//.
    private DATABASE m_hDB; 
    static final int RECORD_MAX_NUM = (1000);
    static final int COLUMN_ID_INDEX = 0;
    static final int COLUMN_NAME_INDEX = 1;
    static final int COLUMN_FPDATA_INDEX = 2;
    static final String[] PROJECTION = new String[] {
    	DATABASE.COLUMN_ID, 	// 0
    	DATABASE.COLUMN_NAME, 	// 1
    	DATABASE.COLUMN_FPDATA, // 2
   	};
    public int m_nDbCnt = 0;
	String[] m_bfDBID = new String[RECORD_MAX_NUM];
	byte[] m_bfDBPtr = new byte[RECORD_MAX_NUM*LAPI.FPINFO_SIZE];

    //------------------------The below defines variables for error code ----------------------------------//
	int m_errCode;
	int m_nCaptureTime, m_nFeatureTime, m_nMatchTime;
    
    //--------------------The below defines object variables for LAPI Library------------------------------//.
    private LAPI m_hLIB;
    public int m_hDev= 0;
    private byte[] m_image = new byte[LAPI.WIDTH*LAPI.HEIGHT];
    private byte[] qr_minutiae = new byte[LAPI.FPINFO_SIZE];
    private byte[] minutiae = new byte[LAPI.FPINFO_SIZE];
	private byte[] itemplate = new byte[LAPI.FPINFO_SIZE];
	private byte[] itemplateToMatch = new byte[LAPI.FPINFO_SIZE];
	private byte[] itemplateToMatched = new byte[LAPI.FPINFO_SIZE];
	private byte[] itemplateToSearch = new byte[LAPI.FPINFO_SIZE];
	int DefFingerTheshold = LAPI.DEF_FINGER_SCORE;
	int DefEnrollTheshold = LAPI.DEF_QUALITY_SCORE;
    int DefMatchTheshold = LAPI.DEF_MATCH_SCORE;
    boolean m_bCancel = false;
	
	//*********************************************************************************************************
	//-----------------------------------------------------------------------------------------------------//
    public HAPI(Activity c, Handler cHandler) {
    	if (c==null) return;
    	m_fHandler = cHandler;
    	m_hLIB = new LAPI(c);
    	m_hDB = new DATABASE(c);
 	}
	//-----------------------------------------------------------------------------------------------------//
    public int GetErrorCode(){
    	return m_errCode;
    }
	//-----------------------------------------------------------------------------------------------------//
    public int GetTheshold(int param){
    	switch(param) {
    	case CONFIG_FINGER_THESHOLD: return DefFingerTheshold;
    	case CONFIG_ENROLL_THESHOLD: return DefEnrollTheshold;
    	case CONFIG_MATCH_THESHOLD: return DefMatchTheshold;
    	}
    	return 0;
    }
	//-----------------------------------------------------------------------------------------------------//
    public void SetTheshold(int param, int newThres){
    	switch(param) {
    	case CONFIG_FINGER_THESHOLD:  	DefFingerTheshold = newThres;     break;
    	case CONFIG_ENROLL_THESHOLD:   DefEnrollTheshold = newThres;     break;
    	case CONFIG_MATCH_THESHOLD:    DefMatchTheshold = newThres;    break;
    	}
    }
	//-----------------------------------------------------------------------------------------------------//
    public void DoCancel(){
    	m_bCancel = true;
    }
	//-----------------------------------------------------------------------------------------------------//
    public int GetProcessTime(int icase){
    	switch (icase) {
    	case 0: return m_nCaptureTime;
    	case 1: return m_nFeatureTime;
    	case 2: return m_nMatchTime;
    	}
    	return 0;
    }
	//-----------------------------------------------------------------------------------------------------//
    void SendMessage (int message,int arg1, int arg2, Object obj) {
    	m_fHandler.obtainMessage(message, arg1, arg2, obj).sendToTarget();
    }
	//*********************************************************************************************************
	// Purpose   : Initialize HAPI & LAPI
	// Function  : OpenDevice
	// Arguments : none
	// Return    : boolean   
	//*********************************************************************************************************
    public boolean OpenDevice() {
       	m_errCode = ERROR_NONE;
		
       	m_hDev = m_hLIB.OpenDeviceEx();
		if (m_hDev==0) {
			m_errCode = ERROR_NONE_DEVICE;
			return false;
		}
		
		return true;
    }
	//*********************************************************************************************************
	// Purpose   : Finalize HAPI & LAPI
	// Function  : CloseDevice
	// Arguments : none
	// Return    : none   
	//*********************************************************************************************************
    public void CloseDevice() {
    	if (m_hDev != 0) {
    		m_hLIB.CloseDeviceEx(m_hDev);
        }
    }
	//*********************************************************************************************************
	// Purpose   : Get image from module 
	// Function  : GetImage
	// Arguments : 
	//			(In) : 
	// Return    : byte[] : return image buffer   
	//*********************************************************************************************************
	public static final int WIDTH  = LAPI.WIDTH;
	public static final int HEIGHT  = LAPI.HEIGHT;
    public byte[] GetImage ()
    {
    	m_nCaptureTime = 0;
        int startTime = (int)System.currentTimeMillis();
        m_hLIB.GetImage(m_hDev,m_image);
        m_nCaptureTime = (int)System.currentTimeMillis() - startTime;
    	return m_image;
    }
	//*********************************************************************************************************
	// Purpose   : Request calibration to module, used after bad image is captured,
    //					Used only for TCS1/TCS2 sensor.
    //                  After this function is called, disconnect module and re-connect it.
    //                  Must wait for a few minutes for calibration of re-connected module.
    //                  After it, Call Initialization function  again.
	// Function  : Calibration
	// Arguments : 
	//			(In) : 
	// Return    : If calibration command is successfully passed, return true else false   
	//*********************************************************************************************************
	public boolean Calibration (int mode)  { 
		if (m_hLIB.Calibration(m_hDev, mode) != 0 ) return false;
		return true; 
	}
    
	
	// The following code is a sample for 1 : N Recognition
	//*********************************************************************************************************
	// Purpose   : Enroll fingerprint in DATABASE
	// Function  : Enroll
	// Arguments : 
	//			(In) : String regId : record tag to be registered in DATABASE
	//			(In) : boolean option : 1 - ANSI format, 0 - ISO format
	// Return    : boolean
	//*********************************************************************************************************
	public boolean Enroll (String regId, boolean option){
		int k, res, itry, qr, cnt;

		m_errCode = ERROR_NONE;
		m_bCancel = false;
		if (m_hDev==0) { m_errCode = ERROR_NONE_DEVICE; return false;}
		if (regId==null || regId.isEmpty()) { m_errCode = ERROR_ARGUMENTS; return false;}
    
		if (FindRecord (regId) != null) { 
    		if (!DeleteRecord (regId)) { m_errCode = ERROR_NEG_DELETE; return false;}
		}
    
		cnt = 0;
		itry = 0;
   		qr = 0;
		for ( k = 0; k < LAPI.FPINFO_SIZE; k ++ ) qr_minutiae[k] = 0;

		SendMessage(MSG_PUT_FINGER,0,0,"");
		while ( itry < 10 ) 
		{
			   //Capture image
    		   int ret = 0;
    		   //m_hLIB.CtrlLed(m_hDev, 1);		//for Optical
    		   while (ret<DefFingerTheshold) {
    			    if (m_bCancel == true) {
    	      			m_errCode = ERROR_DO_CANCELED;
    	      			//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
    				   return false;
    			    }
    			    ret = m_hLIB.GetImage(m_hDev,m_image);
    			    if (ret == LAPI.NOTCALIBRATED) {
    					m_errCode = ERROR_NOT_CALIBRATED;
    		    		//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
    					return false;
    				}
    			    if (ret == LAPI.FALSE) {
    					m_errCode = ERROR_NONE_CAPIMAGE;
    		    		//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
    					return false;
    				}
	    			ret = m_hLIB.IsPressFinger(m_hDev,m_image);
  				}
    		   	//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
   				SendMessage(MSG_FINGER_CAPTURED,LAPI.WIDTH,LAPI.HEIGHT,m_image);
  
	    		//Create Template
	    		for ( k = 0; k < LAPI.FPINFO_SIZE; k ++ ) minutiae[k] = 0;

	    		if (option)
	    			res = m_hLIB.CreateANSITemplate(m_hDev,m_image,minutiae);
	    		else
	    			res = m_hLIB.CreateISOTemplate(m_hDev,m_image,minutiae);

	    		if (qr < res) {
	    			qr = res;
		    		for ( k = 0; k < LAPI.FPINFO_SIZE; k ++ ) qr_minutiae[k] = minutiae[k];
	    		}
	    		else if (qr>0 && qr==res) {
	    			cnt ++;
	    			if (cnt==3) break;
	    		}
	    		else if (qr>0) {
	    			break;
	    		}
				SendMessage(MSG_PUT_FINGER,itry,res,"");
	    		itry ++;
		}
	
		if (qr < DefEnrollTheshold ) {
			m_errCode = ERROR_LOW_QUALITY; 
			return false;
		}
		
		//Register Template as appointed Id 
		return AddNewRecord (regId,qr_minutiae);
	}
    //*********************************************************************************************************
	// Purpose   : Verify fingerprint through 1:1 Matching against to DATABSE 
	// Function  : Verify
	// Arguments : 
	//			(In) : String veriId : record tag to be verified
	//			(In) : boolean option : 1 - ANSI format, 0 - ISO format
	// Return    : boolean   
	//*********************************************************************************************************
    public boolean Verify (String veriId, boolean option){
    	int startTime;
    	m_nCaptureTime = 0;
    	m_nFeatureTime = 0;
    	m_nMatchTime = 0;

    	m_errCode = ERROR_NONE;
		m_bCancel = false;
    	
        if (m_hDev==0) { m_errCode = ERROR_NONE_DEVICE; return false;}
        if (m_nDbCnt==0) { m_errCode = ERROR_EMPTY_DADABASE; return false;}
     	itemplateToMatched = FindRecord (veriId);
    	if (itemplateToMatched == null) { 
        	m_errCode = ERROR_NEG_FIND;
    		return false;
    	}
 	    
       	SendMessage(MSG_PUT_FINGER,0,0,"");
  	    SendMessage(MSG_FINGER_CAPTURED,LAPI.WIDTH,LAPI.HEIGHT,null);
        int ret = 0;
        
        //Capture Image
        //m_hLIB.CtrlLed(m_hDev, 1);		//for Optical
        while (ret<DefFingerTheshold) {
 		    if (m_bCancel == true) {
     			m_errCode = ERROR_DO_CANCELED;
     			//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
			   return false;
		    }
 		   startTime = (int)System.currentTimeMillis();
		    ret = m_hLIB.GetImage(m_hDev,m_image);
		    if (ret == LAPI.NOTCALIBRATED) {
				m_errCode = ERROR_NOT_CALIBRATED;
	    		//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
				return false;
			}
		    if (ret == LAPI.FALSE) {
				m_errCode = ERROR_NONE_CAPIMAGE;
	    		//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
				return false;
			}
        	m_nCaptureTime = (int)System.currentTimeMillis();
        	m_nCaptureTime = m_nCaptureTime - startTime;
     		ret = m_hLIB.IsPressFinger(m_hDev,m_image);
        }
	    //m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
 	    SendMessage(MSG_FINGER_CAPTURED,LAPI.WIDTH,LAPI.HEIGHT,m_image);
        
    	//Create Template
    	int res;
		for ( int k = 0; k < LAPI.FPINFO_SIZE; k ++ ) itemplateToMatch[k] = 0;
        startTime = (int)System.currentTimeMillis();

        if (option)
        	res = m_hLIB.CreateANSITemplate(m_hDev,m_image,itemplateToMatch);
        else
        	res = m_hLIB.CreateISOTemplate(m_hDev,m_image,itemplateToMatch);

		m_nFeatureTime = (int)System.currentTimeMillis();
		m_nFeatureTime = m_nFeatureTime - startTime;
		if (res==0) {
        	m_errCode = ERROR_LOW_QUALITY;
			return false;
		}
				
        //1:1 Matching
        startTime = (int)System.currentTimeMillis();
		res = m_hLIB.CompareTemplates(m_hDev,itemplateToMatch,itemplateToMatched);
		m_nMatchTime = (int)System.currentTimeMillis();
		m_nMatchTime = m_nMatchTime - startTime;
		if (res>=DefMatchTheshold) {
			return true;
		}
		return false;
    }
	//*********************************************************************************************************
	// Purpose   : Identify fingerprint through 1:N Matching against to DATABSE  
	// Function  : Identify
	// Arguments : 
	//			(In) : boolean option : 1 - ANSI format, 0 - ISO format
	// Return    : String   :  record id searched in DATABASE.
	//*********************************************************************************************************
    public String Identify (boolean option){
		int startTime;
		m_nCaptureTime = 0;
		m_nFeatureTime = 0;
		m_nMatchTime = 0;

		m_bCancel = false;
		m_errCode = ERROR_NONE;
		if (m_hDev==0) { m_errCode = ERROR_NONE_DEVICE; return "";}
		if (m_nDbCnt==0) { m_errCode = ERROR_EMPTY_DADABASE; return "";}
		SendMessage(MSG_PUT_FINGER,0,0,"");

		SendMessage(MSG_FINGER_CAPTURED,LAPI.WIDTH,LAPI.HEIGHT,null);
		//Capture Image
		int ret = 0;
		//m_hLIB.CtrlLed(m_hDev, 1);		//for Optical
		while (ret<DefFingerTheshold) {
		   if (m_bCancel == true) {
      			m_errCode = ERROR_DO_CANCELED;
      			//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
			   return "";
		   }
		   startTime = (int)System.currentTimeMillis();
		    ret = m_hLIB.GetImage(m_hDev,m_image);
		    if (ret == LAPI.NOTCALIBRATED) {
				m_errCode = ERROR_NOT_CALIBRATED;
	    		//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
				return "";
			}
		    if (ret == LAPI.FALSE) {
				m_errCode = ERROR_NONE_CAPIMAGE;
	    		//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
				return "";
			}
		   m_nCaptureTime = (int)System.currentTimeMillis();
		   m_nCaptureTime = m_nCaptureTime - startTime;
		   ret = m_hLIB.IsPressFinger(m_hDev,m_image);
		}
		//m_hLIB.CtrlLed(m_hDev, 0);		//for Optical
		SendMessage(MSG_FINGER_CAPTURED,LAPI.WIDTH,LAPI.HEIGHT,m_image);

		//Create Template
		for ( int k = 0; k < LAPI.FPINFO_SIZE; k ++ ) itemplateToSearch[k] = 0;
		startTime = (int)System.currentTimeMillis();

		int res = 0;
		if (option)		//ANSI = 1;
			res = m_hLIB.CreateANSITemplate(m_hDev,m_image,itemplateToSearch);
		else 			//ISO = 0;
			res = m_hLIB.CreateISOTemplate(m_hDev,m_image,itemplateToSearch);

		m_nFeatureTime = (int)System.currentTimeMillis();
		m_nFeatureTime = m_nFeatureTime - startTime;
		if (res==0) {
			m_errCode = ERROR_LOW_QUALITY; 
			return "";
		}
		SendMessage(MSG_ON_SEARCHING,res,0,"");

		startTime = (int)System.currentTimeMillis();

		//1:N Matching
		int index = 0;
		if (option)		//ANSI = 1;
			index = m_hLIB.SearchingANSITemplates(m_hDev,itemplateToSearch, m_nDbCnt,m_bfDBPtr,DefMatchTheshold);
		else 			//ISO = 0;
			index = m_hLIB.SearchingISOTemplates(m_hDev,itemplateToSearch, m_nDbCnt,m_bfDBPtr,DefMatchTheshold);

		m_nMatchTime = (int)System.currentTimeMillis();
		m_nMatchTime = m_nMatchTime - startTime;
		if (index<0) {
			return "";
		}

		String ret_id = m_bfDBID[index];

		return ret_id;
		
    }
	//*********************************************************************************************************
	// Purpose   : Refresh Database
	// Function  : DBRefresh
	// Arguments : none
	// Return    : none   
	//*********************************************************************************************************
    public void DBRefresh() {
    	m_nDbCnt = 0;
    	Cursor c = m_hDB.queryRow(PROJECTION, null, null, null);
    	if ( c != null) m_nDbCnt = c.getCount();
		SendMessage(MSG_DBRECORD_START,m_nDbCnt,0,"");
    	if (m_nDbCnt>0){
        	int i = 0, j;
        	c.moveToFirst();
        	do {
        		String dbname = c.getString(COLUMN_NAME_INDEX);
            	byte[] itemplate = c.getBlob(COLUMN_FPDATA_INDEX);
            	m_bfDBID[i] = dbname;
	    		for (j = 0; j < LAPI.FPINFO_SIZE; j ++) m_bfDBPtr[i*LAPI.FPINFO_SIZE+j] = itemplate[j];
	    		SendMessage(MSG_DBRECORD_NEXT,m_nDbCnt,i,dbname);
            	i ++;
            	if (i==m_nDbCnt) break;
        		c.moveToNext();
        	}
        	while (true);
    	}
		SendMessage(MSG_DBRECORD_END,m_nDbCnt,0,"");
    }
	//*********************************************************************************************************
	// Purpose   : Return record count of DATABASE
	// Function  : GetRecordCount
	// Arguments : none
	// Return    : int   
	//*********************************************************************************************************
    public int GetRecordCount (){
    	return m_nDbCnt;
    }
	//*********************************************************************************************************
	// Purpose   : Clear all record in DATABASE
	// Function  : ClearALLRecords
	// Arguments : none
	// Return    : boolean   
	//*********************************************************************************************************
    public boolean ClearALLRecords () {
    	int ret = m_hDB.deleteRow(null, null);
    	if (ret==0) { m_errCode = ERROR_NEG_ACCESS; return false;}
    	m_nDbCnt = 0;
    	return true;
    }
	//*********************************************************************************************************
	// Purpose   : Delete special record
	// Function  : DeleteRecord
	// Arguments : 
	//			(In) : String delId : 
	// Return    : boolean   
	//*********************************************************************************************************
    public boolean DeleteRecord (String delId) {
    	if (delId==null || delId.isEmpty()) { m_errCode = ERROR_ARGUMENTS; return false;}
    	String where = DATABASE.COLUMN_NAME + " = '" + delId + "'";
    	int ret = m_hDB.deleteRow(where, null);
    	if (ret==0)  { m_errCode = ERROR_NEG_ACCESS; return false;}
    	
    	int i, j;
    	for ( i = 0; i < m_nDbCnt; i ++) {
    		if (m_bfDBID[i].equals(delId)==false) continue;
    		m_bfDBID[i] = m_bfDBID[m_nDbCnt-1];
    		for (j = 0; j < LAPI.FPINFO_SIZE; j ++) 
    			m_bfDBPtr[i*LAPI.FPINFO_SIZE+j] = 
    			m_bfDBPtr[(m_nDbCnt-1)*LAPI.FPINFO_SIZE+j];
    		m_nDbCnt --;
    		return true;
    	}
    	
    	return false;
    }
	//*********************************************************************************************************
	// Purpose   : Add new record in DATABASE
	// Function  : AddNewRecord
	// Arguments : 
	//			(In) : String newId : 
	//			(In) : byte[] itemplate : 
	// Return    : boolean   
	//*********************************************************************************************************
    public boolean AddNewRecord (String newId, byte[] itemplate){
    	if (newId==null || newId.isEmpty()) return false;
    	if (itemplate==null) return false;
    	
    	Cursor c = m_hDB.queryRow(PROJECTION, null, null, null);
    	int recn = c.getCount();
    	if (recn==RECORD_MAX_NUM) { m_errCode = ERROR_OVERFLOW_RECORD; return false;}
        ContentValues v = new ContentValues();
    	v.put(DATABASE.COLUMN_NAME, newId);
    	v.put(DATABASE.COLUMN_FPDATA, itemplate);
    	int ret = m_hDB.insertRow(v);
    	if (ret==0) { m_errCode = ERROR_NEG_ACCESS; return false;}
    	
    	int j;
    	m_bfDBID[m_nDbCnt] = newId;
		for (j = 0; j < LAPI.FPINFO_SIZE; j ++) 
			m_bfDBPtr[m_nDbCnt*LAPI.FPINFO_SIZE+j] = itemplate[j] ;
		m_nDbCnt ++;
    	
    	return true;
    }
	//*********************************************************************************************************
	// Purpose   : Update special record in DATABASE
	// Function  : UpdateRecord
	// Arguments : 
	//			(In) : String updateId : 
	//			(In) : byte[] itemplate : 
	// Return    : boolean   
	// Date      : 2012-05-01  BY YMC IN BEIJING
	//*********************************************************************************************************
    public boolean UpdateRecord (String updateId, byte[] itemplate){
    	if (updateId==null || updateId.isEmpty()) return false;
    	if (itemplate==null) { m_errCode = ERROR_ARGUMENTS; return false;}
    	boolean ret = DeleteRecord(updateId);
    	if (ret) AddNewRecord(updateId,itemplate);
    	return ret;
    }
	//*********************************************************************************************************
	// Purpose   : Find special record in DATABASE
	// Function  : FindRecord
	// Arguments : 
	//			(In) : String findId : 
	// Return    : byte[]   
	//*********************************************************************************************************
    public byte[] FindRecord (String findId){
    	int i, j;
    	if (findId==null || findId.isEmpty()) return null;
    	for ( i = 0; i < m_nDbCnt; i ++) {
    		if (m_bfDBID[i].equals(findId)==false) continue;
    		for (j = 0; j < LAPI.FPINFO_SIZE; j ++) 
    			itemplate[j] = m_bfDBPtr[i*LAPI.FPINFO_SIZE+j];
    		return itemplate;
    	}
    	return null;
    }
}
