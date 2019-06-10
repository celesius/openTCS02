package org.opentcs.testvehicle;
 
import com.google.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLOutput;

import static java.util.Objects.requireNonNull;
 
/**
 * @Description: 通用驱动工厂
 * @Author: Zhang Yangyang
 * @CreateDate: 2019/3/21 16:04
 * @Company: 
 * @Version: 1.0
 */
public class TestCommAdapterFactory implements VehicleCommAdapterFactory {
    private static final Logger LOG = LoggerFactory.getLogger(TestCommAdapterFactory.class);
 
    private TestAdapterComponentsFactory componentsFactory;
    /**
     * 是否初始化
     */
    private boolean initialized;
 
    @Inject
    public TestCommAdapterFactory(TestAdapterComponentsFactory componentsFactory) {
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    }
 
    @Override
    @Deprecated
    public String getAdapterDescription() {
        //这是Kernel中显示的驱动名称,中文会乱码，如果要使用中文，请使用配置文件
        return "ZyyTestAdapter";
    }
 
    @Override
    public boolean providesAdapterFor(Vehicle vehicle) {
        requireNonNull(vehicle, "vehicle");
        return true;
    }
 
    @Override
    public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
        requireNonNull(vehicle, "vehicle");
        return componentsFactory.createCommAdapter(vehicle);
    }

    @Override
    public void initialize() {
        if (initialized) {
            LOG.debug("Already initialized.");
            return;
        }
        initialized = true;
    }
 
    @Override
    public boolean isInitialized() {
        return initialized;
    }
 
    @Override
    public void terminate() {
        if (!initialized) {
            LOG.debug("Not initialized.");
            return;
        }
        initialized = false;
    }
}