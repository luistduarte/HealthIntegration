package pt.ua.ieeta.healthintegration;

public class SettingOptions {
    private String deviceAddress;
    private boolean useThisDevice;
    private boolean recordHeartRate;
    private boolean recordECG;
    private boolean recordAccelerometer;
    private String physicalActivity;

    public SettingOptions(String deviceAddress, boolean useThisDevice, boolean recordHeartRate, boolean recordECG, boolean recordAccelerometer, String physicalActivity) {
        this.deviceAddress = deviceAddress;
        this.useThisDevice = useThisDevice;
        this.recordHeartRate = recordHeartRate;
        this.recordECG = recordECG;
        this.recordAccelerometer = recordAccelerometer;
        this.physicalActivity = physicalActivity;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public boolean isUseThisDevice() {
        return useThisDevice;
    }

    public void setUseThisDevice(boolean useThisDevice) {
        this.useThisDevice = useThisDevice;
    }

    public boolean isRecordHeartRate() {
        return recordHeartRate;
    }

    public void setRecordHeartRate(boolean recordHeartRate) {
        this.recordHeartRate = recordHeartRate;
    }

    public boolean isRecordECG() {
        return recordECG;
    }

    public void setRecordECG(boolean recordECG) {
        this.recordECG = recordECG;
    }

    public boolean isRecordAccelerometer() {
        return recordAccelerometer;
    }

    public void setRecordAccelerometer(boolean recordAccelerometer) {
        this.recordAccelerometer = recordAccelerometer;
    }

    public String getPhysicalActivity() {
        return physicalActivity;
    }

    public void setPhysicalActivity(String physicalActivity) {
        this.physicalActivity = physicalActivity;
    }



}
