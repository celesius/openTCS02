package org.opentcs.testvehicle;
 
import org.opentcs.data.model.Vehicle;
 
/**
 * @Description: 自定义通用驱动工厂
 * @Author: Zhang Yangyang
 * @CreateDate: 2019/3/21 15:30
 * @Company: 
 * @Version: 1.0
 */
public interface TestAdapterComponentsFactory {
    /**
     * 创建自定义驱动
     * @param vehicle
     * @return
     */
    TestCommAdapter createCommAdapter(Vehicle vehicle);
}