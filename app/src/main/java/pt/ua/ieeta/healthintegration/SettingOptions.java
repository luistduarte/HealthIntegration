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

    public boolean isUseThisDevice() {
        return useThisDevice;
    }

    public boolean isRecordHeartRate() {
        return recordHeartRate;
    }

    public boolean isRecordECG() {
        return recordECG;
    }

    public boolean isRecordAccelerometer() {
        return recordAccelerometer;
    }

    public String getPhysicalActivity() {
        return physicalActivity;
    }

}
