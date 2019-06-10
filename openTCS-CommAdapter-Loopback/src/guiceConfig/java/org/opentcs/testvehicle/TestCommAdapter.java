package org.opentcs.testvehicle;
 
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.testvehicle.TestAdapterComponentsFactory;
//import org.opentcs.testvehicle.TestVehicleModel;
import org.opentcs.util.CyclicTask;
import org.opentcs.util.ExplainedBoolean;
 
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
 
import static java.util.Objects.requireNonNull;
 
/**
 * @Description: 自定义通用驱动
 * @Author: Zhang Yangyang
 * @CreateDate: 2019/3/21 15:30
 * @Company: 
 * @Version: 1.0
 */
public class TestCommAdapter extends BasicVehicleCommAdapter {
 
    /**
     * 自定义工厂
     */
    private TestAdapterComponentsFactory componentsFactory;
 
    /**
     * 车
     */
    private Vehicle vehicle;
    /**
     * 是否初始化
     */
    private boolean initialized;
    /**
     * 内部任务类（订单、移动点、路线等）
     */
    private CyclicTask testTask;
 
    private SocketChannel socketchannel;
 
    private Selector selector;
 
    /**
     * 取消订单标志
     */
    private static boolean delOrderFlag = false;
 
    /**
     * 小车绑定ip
     */
    private String clientIp;
 
    /**
     * 小车绑定端口
     */
    private String clientPort;
 
    /**
     * 小车名称
     */
    private String vehicleName;
 
    @Inject
    public TestCommAdapter(TestAdapterComponentsFactory componentsFactory, @Assisted Vehicle vehicle) {
        super(new VehicleProcessModel(vehicle), 2, 1, "CHARGE");
        this.componentsFactory = componentsFactory;
        this.vehicle = vehicle;
        //获取小车参数
        this.clientIp = vehicle.getProperty("IP");
        this.clientPort = vehicle.getProperty("Port");
        this.vehicleName = vehicle.getName();
    }
 
    /**
     * 初始化操作
     */
    @Override
    public void initialize() {
        initialized = true;
        //网络通信,获取当前位置，电量，等信息
        //getProcessModel().setVehicleState(Vehicle.State.IDLE);
        //getProcessModel().setVehiclePosition("Point-0001");
 
    }
 
    /**
     * 开启驱动
     * 对应页面开启按钮
     */
    @Override
    public synchronized void enable() {
        if (isEnabled()) {
            return;
        }
        try {
            //打开监听信道并设置为非阻塞模式
            socketchannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 10005));
            socketchannel.configureBlocking(false);
            //打开并注册到信道
            selector = Selector.open();
            socketchannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            //初始化线程
            testTask = new TestCommAdapter.TestTask();
            //发条绑定客户端IP的消息
            if (clientPort != null && clientIp != null) {
                String msg = clientIp + ":" + clientPort+"###111"+"###"+vehicleName;
                sendMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        Thread simThread = new Thread(testTask, getName() + "-Task");
        //开启线程
        simThread.start();
        super.enable();
 
 
    }
 
    /**
     * 关闭驱动
     */
    @Override
    public synchronized void disable() {
        if (!isEnabled()) {
            return;
        }
        //线程停止
        testTask.terminate();
        testTask = null;
        super.disable();
    }
 
    /**
     * 发送命令
     *
     * @param cmd The command to be sent.
     * @throws IllegalArgumentException
     */
    @Override
    public void sendCommand(MovementCommand cmd)
            throws IllegalArgumentException {
        requireNonNull(cmd, "cmd");
    }
 
    @Override
    public ExplainedBoolean canProcess(List<String> operations) {
        requireNonNull(operations, "operations");
 
        final boolean canProcess = isEnabled();
        final String reason = canProcess ? "" : "adapter not enabled";
        return new ExplainedBoolean(canProcess, reason);
    }
 
    @Override
    public void processMessage(Object message) {
    }
 
    @Override
    protected void connectVehicle() {
 
    }
 
    @Override
    protected void disconnectVehicle() {
 
    }
 
    @Override
    protected boolean isVehicleConnected() {
        return true;
    }
 
    /**
     * 内部类，用于处理运行步骤
     */
    private class TestTask extends CyclicTask {
        private TestTask() {
            super(0);
        }
 
        /**
         * 线程执行
         */
        @Override
        protected void runActualTask() {
            System.out.println("runActualTask...");
            //收到消息再处理
//            String message = getMessage();
//            if (message != null && message != "") {
//                System.out.println(message);
//                JSONObject messageJson = JSONObject.parseObject(message);
//                String cmd = messageJson.getString("cmd");
//                if (CmdConstant.POINT_ARRIVAL_NTF.equals(cmd)){
//                    String desc = messageJson.getString("destination");
//                    //设置车点
//                    getProcessModel().setVehiclePosition(desc);
//                    //设置状态
//                    getProcessModel().setVehicleState(Vehicle.State.IDLE);
//                    delOrderFlag = false;
//                    //返回收到结果
//                    JSONObject backRsp = new JSONObject();
//                    backRsp.put("cmd","point_arrival_rsp");
//                    backRsp.put("destination","ok");
//                    sendMessage(clientIp + ":" + clientPort + "###" +backRsp.toJSONString()+"###"+vehicleName);
//                }else if (CmdConstant.DELETE_ORDER_CMD.equals(cmd)||
//                        CmdConstant.DELETE_ORDER_CMD2.equals(cmd)){
//                    delOrderFlag = true;
//                    //返回收到结果
//                    JSONObject backRsp = new JSONObject();
//                    backRsp.put("cmd","delete_order_rsp");
//                    backRsp.put("destination","ok");
//                    sendMessage(clientIp + ":" + clientPort + "###" +backRsp.toJSONString()+"###"+vehicleName);
//                }else if (CmdConstant.BACK_WAIT_CMD.equals(cmd)){
//                    //返回收到结果
//                    JSONObject backRsp = new JSONObject();
//                    backRsp.put("cmd","back_wait_rsp");
//                    backRsp.put("destination","ok");
//                    sendMessage(clientIp + ":" + clientPort + "###" +backRsp.toJSONString()+"###"+vehicleName);
//                }else if (CmdConstant.ORDER_APPLY_CMD.equals(cmd)){
//                    //返回收到结果
//                    JSONObject backRsp = new JSONObject();
//                    backRsp.put("cmd","navi_order_rsp");
//                    backRsp.put("destination","ok");
//                    sendMessage(clientIp + ":" + clientPort + "###" +backRsp.toJSONString()+"###"+vehicleName);
//                }
//            }
//            //移动命令
//            final MovementCommand curCommand;
//            //synchronized防止驱动冲突
//            synchronized (TestCommAdapter.this) {
//                //拿到队列中的命令
//                curCommand = getSentQueue().peek();
//            }
//            if (curCommand != null) {
//                //获得命令中下一步的点
//                final Route.Step curStep = curCommand.getStep();
//                try {
//                    simulateMovement(curStep);
//                    if (!curCommand.isWithoutOperation()) {
//                        simulateOperation(curCommand.getOperation());
//                    }
//                    if (!isTerminated()) {
//                        if (getSentQueue().size() <= 1 && getCommandQueue().isEmpty()) {
//                            getProcessModel().setVehicleState(Vehicle.State.IDLE);
//                        }
//                        synchronized (TestCommAdapter.this) {
//                            MovementCommand sentCmd = getSentQueue().poll();
//                            if (sentCmd != null && sentCmd.equals(curCommand)) {
//                                getProcessModel().commandExecuted(curCommand);
//                                TestCommAdapter.this.notify();
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
 
        /**
         * 移动方法
         *
         * @param step
         * @throws Exception
         */
        private void simulateMovement(Route.Step step) throws Exception {
            System.out.println("simulateMovement...");
            //没有路径或是取消订单命令
//            if (step.getPath() == null || delOrderFlag) {
//                return;
//            }
//            Vehicle.Orientation orientation = step.getVehicleOrientation();
//            long pathLength = step.getPath().getLength();
//            int maxVelocity;
//            switch (orientation) {
//                case BACKWARD:
//                    maxVelocity = step.getPath().getMaxReverseVelocity();
//                    break;
//                default:
//                    maxVelocity = step.getPath().getMaxVelocity();
//                    break;
//            }
//            String pointName = step.getDestinationPoint().getName();
//            getProcessModel().setVehicleState(Vehicle.State.EXECUTING);
//            String currentPoint = "";
//            String currentStatus = "";
//            boolean flag = false;
//            boolean delInnerOrderFlag = false;
//
//            while (!flag) {
//                String str = clientIp + ":" + clientPort + "###" +
//                        "{\"cmd\":\"next_point_req\", \"destination\":\""+pointName+"\"}"+"###"+vehicleName;
//                //发送消息
//                sendMessage(str);
//                String responseMessage = getMessage();
//                //机器人返回接收情况
//                if (responseMessage != null && responseMessage != ""){
//                    JSONObject responseJson = JSONObject.parseObject(responseMessage);
//                    String cmd = responseJson.getString("cmd");
//                    //取消订单命令，直接跳出循环
//                    if (CmdConstant.DELETE_ORDER_CMD.equals(cmd) ||
//                            CmdConstant.DELETE_ORDER_CMD2.equals(cmd)){
//                        delInnerOrderFlag = true;
//                        //返回收到结果
//                        JSONObject backRsp = new JSONObject();
//                        backRsp.put("cmd","delete_order_rsp");
//                        backRsp.put("destination","ok");
//                        sendMessage(clientIp + ":" + clientPort + "###" +backRsp.toJSONString()+"###"+vehicleName);
//                        break;
//                    }else if (CmdConstant.NEXT_POINT_RSP.equals(cmd)){
//                        delInnerOrderFlag = false;
//                        String desc = responseJson.getString("result");
//                        if (desc.equals("ok")){
//                            flag = true;
//                        }
//                    }
//                }
//                Thread.sleep(1000);
//            }
//
//            //已经发过取消订单命令，就不需要再请求
//            if (!delInnerOrderFlag){
//                //判断是否到了下一个点，没有则一直请求
//                while (!currentPoint.equals(pointName) && !isTerminated()) {
//                    String str = clientIp + ":" + clientPort + "###" +
//                            "{\"cmd\":\"robot_status_req\", \"reserved\":\"unknown\"}"+"###"+vehicleName;
//                    //发送消息，暂时不处理返回结果
//                    sendMessage(str);
//                    String responseMessage = getMessage();
//                    if (responseMessage != null && responseMessage != ""){
//                        JSONObject responseJson = JSONObject.parseObject(responseMessage);
//                        String cmd = responseJson.getString("cmd");
//                        //内部收到取消订单命令
//                        if (CmdConstant.DELETE_ORDER_CMD.equals(cmd)||
//                                CmdConstant.DELETE_ORDER_CMD2.equals(cmd)){
//                            //返回收到结果
//                            JSONObject backRsp = new JSONObject();
//                            backRsp.put("cmd","delete_order_rsp");
//                            backRsp.put("destination","ok");
//                            sendMessage(clientIp + ":" + clientPort + "###" +backRsp.toJSONString()+"###"+vehicleName);
//                            break;
//                        }else if (CmdConstant.POINT_ARRIVAL_NTF.equals(cmd)){
//                            String desc = responseJson.getString("destination");
//                            currentPoint = desc;
//                            //设置状态
//                            getProcessModel().setVehicleState(Vehicle.State.IDLE);
//                            //设置车点
//                            getProcessModel().setVehiclePosition(desc);
//                        }else {
//                            Thread.sleep(1000);
//                            continue;
//                        }
//                    }else {
//                        Thread.sleep(1000);
//                        continue;
//                    }
//                }
//            }
        }
 
        private void simulateOperation(String operation) throws Exception {
            requireNonNull(operation, "operation");
            if (isTerminated()) {
                return;
            }
        }
 
        /**
         * 读取消息
         *
         * @return
         */
        private String getMessage() {
            String receivedString = "";
            try {
                if (selector.select() > 0) {
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isReadable()) {
                            //使用NIO channel中的数据
                            SocketChannel clientchannel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            clientchannel.read(buffer);
                            buffer.flip();
                            //将字节转化为UTF-8的字符串
                            receivedString = Charset.forName("UTF-8").newDecoder().decode(buffer).toString();
                            //为下次读取准备
                            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                        //删除正在处理的selectionkey
                        selector.selectedKeys().remove(key);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return receivedString;
        }
 
    }
 
    /**
     * 发送消息
     *
     * @param message
     * @throws IOException
     */
    private void sendMessage(String message) {
        try {
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
            socketchannel.write(writeBuffer);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
}