package com.ismummy.rsaenrollment.HEROFUN;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.ismummy.rsaenrollment.HEROFUN.HostUsb;

import android.app.Activity;
import android.util.Log;

public class LAPI {
    static final String TAG = "LAPI";
    //****************************************************************************************************
	static 
	{
		try{
			System.loadLibrary("biofp_e_lapi");
		}
		catch(UnsatisfiedLinkError e) {
			Log.e("LAPI","biofp_e_lapi",e);
		}
	}
	//****************************************************************************************************
	static final File PWFILE_BH701 = new File("/sys/class/power_supply/usb/device/CONTROL_GPIO114");
	static final File PWFILE_BH502 = new File("/sys/class/power_supply/usb/device/CONTROL_GPIO83");
	//****************************************************************************************************
	public static final int VID = 0x0483;
	public static final int SCSI_PID = 0x5710;
	public static final int BULK_PID = 0x5720;
	private static HostUsb m_usbHost = null;
	private static int m_hUSB = 0;
    public static final int MSG_OPEN_DEVICE = 0x10;
    public static final int MSG_CLOSE_DEVICE = 0x11;
    public static final int MSG_BULK_TRANS_IN = 0x12;
    public static final int MSG_BULK_TRANS_OUT = 0x13;
	//****************************************************************************************************
	public static final int WIDTH  = 256;
	public static final int HEIGHT  = 360;
	public static final int IMAGE_SIZE = WIDTH*HEIGHT;
    //****************************************************************************************************
	public static final int FPINFO_STD_MAX_SIZE = 1024;
    public static final int DEF_FINGER_SCORE = 45;
    public static final int DEF_QUALITY_SCORE = 30;
    public static final int DEF_MATCH_SCORE = 45;
	public static final int FPINFO_SIZE = FPINFO_STD_MAX_SIZE;
    //****************************************************************************************************
    public static final int TRUE = 1;
    public static final int FALSE = 0;
    public static final int NOTCALIBRATED = -2;
    //****************************************************************************************************
    public static final int SCSI_MODE = 1;
    public static final int SPI_MODE = 2;
    public static final int BULK_MODE = 3;
    public static final int VERSION1 = 1;
    public static final int VERSION2 = 2;
    public static final int TABLET_DEFAULT = 0;
    public static final int TABLET_BH701 = 1;
    public static final int TABLET_BH502 = 2;
    public static final int commMode = SPI_MODE;
    public static final int versionNo = VERSION2;
    public static final int tabletNo = TABLET_DEFAULT;
    //****************************************************************************************************
    private static Activity m_content = null;
    //****************************************************************************************************
	private static int CallBack (int message, int notify, int param, Object data)
	{
		switch (message) {
		case MSG_OPEN_DEVICE:
			if (commMode == SCSI_MODE) m_usbHost = new HostUsb (m_content, VID, SCSI_PID);
			else m_usbHost = new HostUsb (m_content, VID, BULK_PID);

			if (m_usbHost != null) {
				m_usbHost.WaitForInterfaces(); 
			    m_hUSB = m_usbHost.OpenDeviceInterfaces();
				if (m_hUSB<0) {
					return 0;
				}
			}
			return m_hUSB;
		case MSG_CLOSE_DEVICE:
			if (m_usbHost != null) {
				m_usbHost.CloseDeviceInterface();
				m_hUSB = -1;
			}
			return 1;
		case MSG_BULK_TRANS_IN:
			if (m_usbHost.USBBulkReceive((byte[])data,notify,param)) return notify;
			return 0;
		case MSG_BULK_TRANS_OUT:
			if (m_usbHost.USBBulkSend((byte[])data,notify,param)) return notify;
			return 0;
		}
		return 0;
	}
    //****************************************************************************************************
	public LAPI(Activity a) {
		m_content = a;
	}
	//****************************************************************************************************
	protected void POWER_ON(int tablet)
	{
		FileReader inCmd; 
		try {
			if (tablet == TABLET_BH701) inCmd = new FileReader(PWFILE_BH701);
			else if (tablet == TABLET_BH502) inCmd = new FileReader(PWFILE_BH502);
			else return;
	      	 inCmd.read();
	      	 inCmd.close();
	       }
	    catch (Exception e) {}

		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {}
	}
    //****************************************************************************************************
	protected void POWER_OFF(int tablet)
	{
		FileWriter closefr;
        try {
			if (tablet == TABLET_BH701) closefr = new FileWriter(PWFILE_BH701);
			else if (tablet == TABLET_BH502) closefr = new FileWriter(PWFILE_BH502);
			else return;
            closefr.write("1"); 
            closefr.close();
        }
        catch (Exception e) {}

        try {
			Thread.sleep(30);
		} catch (InterruptedException e) {}
	}
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function returns string version of the Fingerprint Recognition SDK.
	// Function  : GetVersion
	// Arguments : void
	// Return    : String 
	//			     return string version of the Fingerprint Recognition SDK. 	
	//------------------------------------------------------------------------------------------------//
	public native String GetVersion();
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function controls background led of fingerprint scanner.
	// Function  : CtrlLed
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//      (In) : int bSwitch : on/off
	// Return    : int
	//			      If successful, return 1, else 0 	
	//------------------------------------------------------------------------------------------------//
	public native int CtrlLed(int device, int bSwitch);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function initializes the Fingerprint Recognition SDK Library and 
	//				connects Fingerprint Collection Module.
 	// Function  : OpenDevice
	// Arguments : void
	// Return    : int  
	//			     If successful, return handle of device, else 0. 	
	//------------------------------------------------------------------------------------------------//
	private native int OpenDevice(int commMode, int versionNo);
	public int OpenDeviceEx()
	{
		int ret = 0;
		POWER_ON(tabletNo);
		ret = OpenDevice(commMode, versionNo);
		if (ret == 0) POWER_OFF(tabletNo);
		return ret;
	}
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function finalizes the Fingerprint Recognition SDK Library and 
	//				disconnects Fingerprint Collection Module.
 	// Function  : CloseDevice
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	// Return    : int
	//			      If successful, return 1, else 0 	
	//------------------------------------------------------------------------------------------------//
	private  native int CloseDevice(int device);
	public int CloseDeviceEx(int device)
	{
		int ret;
		ret = CloseDevice(device);
		POWER_OFF(tabletNo);
		return ret;
	}
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function returns image captured from Fingerprint Collection Module.
	// Function  : GetImage
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//  (In/Out) : byte[] image : image captured from this device
	// Return    : int
	//			      If successful, return 1,
	//				  if not calibrated(TCS1/2), return -2,		
	//						else, return  0 	
	//------------------------------------------------------------------------------------------------//
	public native int GetImage(int device, byte[] image);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function does calibration of this Fingerprint Collection Module.
	//			   This function is used only for TCS1/TCS2 Sensor.
	// Function  : Calibration
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//      (In) : int mode : dry/default/wet
	// Return    :  
	//			   int :   If successful, return 1, else 0 	
	//------------------------------------------------------------------------------------------------//
	public native int Calibration(int device, int mode);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function checks whether finger is on sensor of this device or not.
	// Function  : IsPressFinger
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//		(In) : byte[] image : image returned from function "GetImage()"
	// Return    : int 
	//				   return percent value indicating that finger is placed on sensor(0~100). 	
	//------------------------------------------------------------------------------------------------//
	public native int IsPressFinger(int device,byte[] image);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function creates the ANSI standard template from the uncompressed raw image. 
	// Function  : CreateANSITemplate
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//		(In) : byte[] image : image returned from function "GetImage()"
	//	(In/Out) : byte[] itemplate : ANSI standard template created from image.
	// Return    : int : 
	//				   If this function successes, return none-zero, else 0. 	
	//------------------------------------------------------------------------------------------------//
	public native int CreateANSITemplate(int device,byte[] image, byte[] itemplate);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function creates the ISO standard template from the uncompressed raw image. 
	// Function  : CreateISOTemplate
	// Arguments : void
	//      (In) : int device : handle returned from function "OpenDevice()"
	//		(In) : byte[] image : image returned from function "GetImage()"
	//  (In/Out) : byte[] itemplate : ISO standard template created from image.
	// Return    : int : 
	//				   If this function successes, return none-zero, else 0. 	
	//------------------------------------------------------------------------------------------------//
	public native int CreateISOTemplate(int device,byte[] image,  byte[] itemplate);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function gets the quality value of fingerprint raw image. 
	// Function  : GetImageQuality
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//		(In) : byte[] image : image returned from function "GetImage()"
	// Return    : int : 
	//				   return quality value(0~100) of fingerprint raw image. 	
	//------------------------------------------------------------------------------------------------//
	public native int GetImageQuality(int device,byte[] image);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function gets the NFI quality value of fingerprint raw image. 
	// Function  : GetNFIQuality
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//		(In) : byte[] image : image returned from function "GetImage()"
	// Return    : int : 
	//				   return NFI quality value(1~5) of fingerprint raw image. 	
	//------------------------------------------------------------------------------------------------//
	public native int GetNFIQuality(int device,byte[] image);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function matches two templates and returns similar match score.
	//             This function is for 1:1 Matching and only used in fingerprint verification. 
	// Function  : CompareTemplates
	// Arguments : 
	//      	(In) : int device : handle returned from function "OpenDevice()"
	//			(In) : byte[] itemplateToMatch : template to match : 
	//                 This template must be used as that is created by function "CreateANSITemplate()"  
	//                 or function "CreateISOTemplate()".
	//			(In) : byte[] itemplateToMatched : template to be matched
	//                 This template must be used as that is created by function "CreateANSITemplate()"  
	//                 or function "CreateISOTemplate()".
	// Return    : int 
	//					return similar match score(0~100) of two fingerprint templates.
	//------------------------------------------------------------------------------------------------//
	public native int CompareTemplates(int device,byte[] itemplateToMatch, byte[] itemplateToMatched);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function matches the appointed ANSI template against to ANSI template array of DATABASE.
	//             This function is for 1:N Matching and only used in fingerprint identification. 
	// Function  : SearchingANSITemplates
	// Arguments : 
	//      	(In) : int device : handle returned from function "OpenDevice()"
	//			(In) : byte[] itemplateToSearch : template to search
	//                 This template must be used as that is created by function "CreateANSITemplate()".  
	//			(In) : byte[] numberOfDbTemplates : number of templates to be searched.
	//			(In) : byte[] arrayOfDbTemplates : template array to be searched.
	//                 These templates must be used as that is created by function "CreateANSITemplate()".  
	//			(In) : int scoreThreshold : 
	//                 This argument is the threshold of similar match score for 1: N Matching.
	// Return    : int 
	//				   If successful, return index number of template searched inside template array, 
	//				   else -1. 	
	//------------------------------------------------------------------------------------------------//
	public native int SearchingANSITemplates(int device, byte[] itemplateToSearch, 
		   	int numberOfDbTemplates, byte[] arrayOfDbTemplates, int scoreThreshold);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function matches the appointed ISO template against to ISO template array of DATABASE.
	//             This function is for 1:N Matching and only used in fingerprint identification. 
	// Function  : SearchingISOTemplates
	// Arguments : 
	//      	(In) : int device : handle returned from function "OpenDevice()"
	//			(In) : byte[] itemplateToSearch : template to search
	//                 This template must be used as that is created by function "CreateISOTemplate()".  
	//			(In) : byte[] numberOfDbTemplates : number of templates to be searched.
	//			(In) : byte[] arrayOfDbTemplates : template array to be searched.
	//                 These templates must be used as that is created by function "CreateISOTemplate()".  
	//			(In) : int scoreThreshold : 
	//                 This argument is the threshold of similar match score for 1: N Matching.
	// Return    : int 
	//				   If successful, return index number of template searched inside template array, 
	//				   else -1. 	
	//------------------------------------------------------------------------------------------------//
	public native int SearchingISOTemplates(int device, byte[] itemplateToSearch, 
		   	int numberOfDbTemplates, byte[] arrayOfDbTemplates, int scoreThreshold);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function compresses raw fingerprint image by WSQ algorithm
	// Function  : CompressToWSQImage
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//		(In) : byte[] rawImage : fingerprint raw image
	//	(In/Out) : byte[] wsqImage : fingerprint image to be compressed by WSQ algorithm
	// Return    : long 
	//					return size of image compressed by WSQ
	//------------------------------------------------------------------------------------------------//
	public native long  CompressToWSQImage (int device,byte[] rawImage, byte[] wsqImage);
	//------------------------------------------------------------------------------------------------//
	// Purpose   : This function uncompresses wsq fingerprint image by WSQ algorithm
	// Function  : UnCompressFromWSQImage
	// Arguments : 
	//      (In) : int device : handle returned from function "OpenDevice()"
	//		(In) : byte[] wsqImage : compressed fingerprint image
	//		(In) : long wsqSize : compressed image size
	//	(In/Out) : byte[] rawImage : fingerprint image to be uncompressed
	// Return    : long 
	//				return size of uncompressed image
	//------------------------------------------------------------------------------------------------//
	public native long  UnCompressFromWSQImage (int device,byte[] wsqImage, long wsqSize, byte[] rawImage);
	//****************************************************************************************************
}
