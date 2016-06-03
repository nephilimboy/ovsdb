/*
 * Copyright (c) 2015 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.ovsdb.utils.southbound.utils;

import com.google.common.collect.ImmutableBiMap;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.ovsdb.utils.config.ConfigProperties;
import org.opendaylight.ovsdb.utils.mdsal.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.bridge.attributes.BridgeExternalIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.bridge.attributes.BridgeOtherConfigs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.bridge.attributes.ControllerEntry;import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.bridge.attributes.ControllerEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.bridge.attributes.ProtocolEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.bridge.attributes.ProtocolEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.node.attributes.ConnectionInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.node.attributes.ConnectionInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.node.attributes.ManagerEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.InterfaceExternalIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.InterfaceExternalIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.InterfaceExternalIdsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.Options;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.OptionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.OptionsKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SouthboundUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SouthboundUtils.class);
    private static final int OVSDB_UPDATE_TIMEOUT = 1000;
    public static final TopologyId OVSDB_TOPOLOGY_ID = new TopologyId(new Uri("ovsdb:1"));
    private final MdsalUtils mdsalUtils;
    public static final String OPENFLOW_CONNECTION_PROTOCOL = "tcp";
    public static short OPENFLOW_PORT = 6653;
    public static final String OVSDB_URI_PREFIX = "ovsdb";
    public static final String BRIDGE_URI_PREFIX = "bridge";

    public SouthboundUtils(MdsalUtils mdsalUtils) {
        this.mdsalUtils = mdsalUtils;
    }

    public static final ImmutableBiMap<String, Class<? extends InterfaceTypeBase>> OVSDB_INTERFACE_TYPE_MAP
            = new ImmutableBiMap.Builder<String, Class<? extends InterfaceTypeBase>>()
            .put("internal", InterfaceTypeInternal.class)
            .put("vxlan", InterfaceTypeVxlan.class)
            .put("vxlan-gpe", InterfaceTypeVxlanGpe.class)
            .put("patch", InterfaceTypePatch.class)
            .put("system", InterfaceTypeSystem.class)
            .put("tap", InterfaceTypeTap.class)
            .put("geneve", InterfaceTypeGeneve.class)
            .put("gre", InterfaceTypeGre.class)
            .put("ipsec_gre", InterfaceTypeIpsecGre.class)
            .put("gre64", InterfaceTypeGre64.class)
            .put("ipsec_gre64", InterfaceTypeIpsecGre64.class)
            .put("lisp", InterfaceTypeLisp.class)
            .put("dpdk", InterfaceTypeDpdk.class)
            .put("dpdkr", InterfaceTypeDpdkr.class)
            .put("dpdkvhost", InterfaceTypeDpdkvhost.class)
            .put("dpdkvhostuser", InterfaceTypeDpdkvhostuser.class)
            .build();

    public static final ImmutableBiMap<Class<? extends OvsdbBridgeProtocolBase>,String> OVSDB_PROTOCOL_MAP
            = new ImmutableBiMap.Builder<Class<? extends OvsdbBridgeProtocolBase>,String>()
            .put(OvsdbBridgeProtocolOpenflow10.class,"OpenFlow10")
            .put(OvsdbBridgeProtocolOpenflow11.class,"OpenFlow11")
            .put(OvsdbBridgeProtocolOpenflow12.class,"OpenFlow12")
            .put(OvsdbBridgeProtocolOpenflow13.class,"OpenFlow13")
            .put(OvsdbBridgeProtocolOpenflow14.class,"OpenFlow14")
            .put(OvsdbBridgeProtocolOpenflow15.class,"OpenFlow15")
            .build();

    public static NodeId createNodeId(IpAddress ip, PortNumber port) {
        String uriString = OVSDB_URI_PREFIX + "://"
                + String.valueOf(ip.getValue()) + ":" + port.getValue();
        Uri uri = new Uri(uriString);
        return new NodeId(uri);
    }

    public static Node createNode(ConnectionInfo key) {
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setNodeId(createNodeId(key.getRemoteIp(), key.getRemotePort()));
        nodeBuilder.addAugmentation(OvsdbNodeAugmentation.class, createOvsdbAugmentation(key));
        return nodeBuilder.build();
    }

    public static OvsdbNodeAugmentation createOvsdbAugmentation(ConnectionInfo key) {
        OvsdbNodeAugmentationBuilder ovsdbNodeBuilder = new OvsdbNodeAugmentationBuilder();
        ovsdbNodeBuilder.setConnectionInfo(key);
        return ovsdbNodeBuilder.build();
    }

    public static InstanceIdentifier<Node> createInstanceIdentifier(NodeId nodeId) {
        return InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(OVSDB_TOPOLOGY_ID))
                .child(Node.class,new NodeKey(nodeId));
    }

    public static InstanceIdentifier<Node> createInstanceIdentifier(NodeKey ovsdbNodeKey, String bridgeName) {
        return createInstanceIdentifier(createManagedNodeId(ovsdbNodeKey.getNodeId(), bridgeName));
    }

    public static NodeId createManagedNodeId(NodeId ovsdbNodeId, String bridgeName) {
        return new NodeId(ovsdbNodeId.getValue()
                + "/" + BRIDGE_URI_PREFIX + "/" + bridgeName);
    }

    public static InstanceIdentifier<Node> createInstanceIdentifier(ConnectionInfo key) {
        return createInstanceIdentifier(key.getRemoteIp(), key.getRemotePort());
    }

    public static InstanceIdentifier<Node> createInstanceIdentifier(IpAddress ip, PortNumber port) {
        InstanceIdentifier<Node> path = InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(OVSDB_TOPOLOGY_ID))
                .child(Node.class,createNodeKey(ip,port));
        LOG.debug("Created ovsdb path: {}",path);
        return path;
    }

    public static InstanceIdentifier<Node> createInstanceIdentifier(ConnectionInfo key,OvsdbBridgeName bridgeName) {
        return createInstanceIdentifier(createManagedNodeId(key, bridgeName));
    }

    public static InstanceIdentifier<Node> createInstanceIdentifier(ConnectionInfo key, String bridgeName) {
        return createInstanceIdentifier(key, new OvsdbBridgeName(bridgeName));
    }

    public InstanceIdentifier<TerminationPoint> createTerminationPointInstanceIdentifier(Node node, String portName){

        InstanceIdentifier<TerminationPoint> terminationPointPath = InstanceIdentifier
                .create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(OVSDB_TOPOLOGY_ID))
                .child(Node.class,node.getKey())
                .child(TerminationPoint.class, new TerminationPointKey(new TpId(portName)));

        LOG.debug("Termination point InstanceIdentifier generated : {}",terminationPointPath);
        return terminationPointPath;
    }

    public static NodeKey createNodeKey(IpAddress ip, PortNumber port) {
        return new NodeKey(createNodeId(ip, port));
    }

    public static NodeId createManagedNodeId(ConnectionInfo key, OvsdbBridgeName bridgeName) {
        return createManagedNodeId(key.getRemoteIp(), key.getRemotePort(), bridgeName);
    }

    public static NodeId createManagedNodeId(IpAddress ip, PortNumber port, OvsdbBridgeName bridgeName) {
        return new NodeId(createNodeId(ip,port).getValue()
                + "/" + BRIDGE_URI_PREFIX + "/" + bridgeName.getValue());
    }

    public static NodeId createManagedNodeId(InstanceIdentifier<Node> iid) {
        NodeKey nodeKey = iid.firstKeyOf(Node.class);
        return nodeKey.getNodeId();
    }

    public ConnectionInfo getConnectionInfo(Node ovsdbNode) {
        ConnectionInfo connectionInfo = null;
        OvsdbNodeAugmentation ovsdbNodeAugmentation = extractOvsdbNode(ovsdbNode);
        if (ovsdbNodeAugmentation != null) {
            connectionInfo = ovsdbNodeAugmentation.getConnectionInfo();
        }
        return connectionInfo;
    }

    public OvsdbNodeAugmentation extractOvsdbNode(Node node) {
        return node.getAugmentation(OvsdbNodeAugmentation.class);
    }

    public static IpAddress createIpAddress(InetAddress address) {
        IpAddress ip = null;
        if (address instanceof Inet4Address) {
            ip = createIpAddress((Inet4Address)address);
        } else if (address instanceof Inet6Address) {
            ip = createIpAddress((Inet6Address)address);
        }
        return ip;
    }

    public static IpAddress createIpAddress(Inet4Address address) {
        return IetfInetUtil.INSTANCE.ipAddressFor(address);
    }

    public static IpAddress createIpAddress(Inet6Address address) {
        Ipv6Address ipv6 = new Ipv6Address(address.getHostAddress());
        return new IpAddress(ipv6);
    }

    public static ConnectionInfo getConnectionInfo(final String addressStr, final String portStr) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(addressStr);
        } catch (UnknownHostException e) {
            LOG.warn("Could not allocate InetAddress", e);
        }

        IpAddress address = createIpAddress(inetAddress);
        PortNumber port = new PortNumber(Integer.parseInt(portStr));

        LOG.info("connectionInfo: {}", new ConnectionInfoBuilder()
                .setRemoteIp(address)
                .setRemotePort(port)
                .build());
        return new ConnectionInfoBuilder()
                .setRemoteIp(address)
                .setRemotePort(port)
                .build();
    }

    public static String connectionInfoToString(final ConnectionInfo connectionInfo) {
        return String.valueOf(
                connectionInfo.getRemoteIp().getValue()) + ":" + connectionInfo.getRemotePort().getValue();
    }

    public boolean addOvsdbNode(final ConnectionInfo connectionInfo) {
        return addOvsdbNode(connectionInfo, OVSDB_UPDATE_TIMEOUT);
    }

    public boolean addOvsdbNode(final ConnectionInfo connectionInfo, long timeout) {
        boolean result = mdsalUtils.put(LogicalDatastoreType.CONFIGURATION,
                createInstanceIdentifier(connectionInfo),
                createNode(connectionInfo));
        if (timeout != 0) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting after adding OVSDB node {}",
                        connectionInfoToString(connectionInfo), e);
            }
        }
        return result;
    }

    public Node getOvsdbNode(final ConnectionInfo connectionInfo) {
        return mdsalUtils.read(LogicalDatastoreType.OPERATIONAL,
                createInstanceIdentifier(connectionInfo));
    }

    public boolean deleteOvsdbNode(final ConnectionInfo connectionInfo) {
        return deleteOvsdbNode(connectionInfo, OVSDB_UPDATE_TIMEOUT);
    }

    public boolean deleteOvsdbNode(final ConnectionInfo connectionInfo, long timeout) {
        boolean result = mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION,
                createInstanceIdentifier(connectionInfo));
        if (timeout != 0) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting after deleting OVSDB node {}",
                        connectionInfoToString(connectionInfo), e);
            }
        }
        return result;
    }

    public Node connectOvsdbNode(final ConnectionInfo connectionInfo) {
        return connectOvsdbNode(connectionInfo, OVSDB_UPDATE_TIMEOUT);
    }

    public Node connectOvsdbNode(final ConnectionInfo connectionInfo, long timeout) {
        addOvsdbNode(connectionInfo, timeout);
        Node node = getOvsdbNode(connectionInfo);
        LOG.info("Connected to {}", connectionInfoToString(connectionInfo));
        return node;
    }

    public boolean disconnectOvsdbNode(final ConnectionInfo connectionInfo) {
        return disconnectOvsdbNode(connectionInfo, OVSDB_UPDATE_TIMEOUT);
    }

    public boolean disconnectOvsdbNode(final ConnectionInfo connectionInfo, long timeout) {
        deleteOvsdbNode(connectionInfo, timeout);
        LOG.info("Disconnected from {}", connectionInfoToString(connectionInfo));
        return true;
    }

    public List<ControllerEntry> createControllerEntry(String controllerTarget) {
        List<ControllerEntry> controllerEntriesList = new ArrayList<>();
        controllerEntriesList.add(new ControllerEntryBuilder()
                .setTarget(new Uri(controllerTarget))
                .build());
        return controllerEntriesList;
    }

    /**
     * Extract the <code>store</code> type data store contents for the particular bridge identified by
     * <code>bridgeName</code>.
     *
     * @param connectionInfo address for the node
     * @param bridgeName name of the bridge
     * @param store defined by the <code>LogicalDatastoreType</code> enumeration
     * @return <code>store</code> type data store contents
     */
    public OvsdbBridgeAugmentation getBridge(ConnectionInfo connectionInfo, String bridgeName,
                                              LogicalDatastoreType store) {
        OvsdbBridgeAugmentation ovsdbBridgeAugmentation = null;
        Node bridgeNode = getBridgeNode(connectionInfo, bridgeName, store);
        if (bridgeNode != null) {
            ovsdbBridgeAugmentation = bridgeNode.getAugmentation(OvsdbBridgeAugmentation.class);
        }
        return ovsdbBridgeAugmentation;
    }

    /**
     * extract the <code>LogicalDataStoreType.OPERATIONAL</code> type data store contents for the particular bridge
     * identified by <code>bridgeName</code>
     *
     * @param connectionInfo address for the node
     * @param bridgeName name of the bridge
     * @see <code>NetvirtIT.getBridge(ConnectionInfo, String, LogicalDatastoreType)</code>
     * @return <code>LogicalDatastoreType.OPERATIONAL</code> type data store contents
     */
    public OvsdbBridgeAugmentation getBridge(ConnectionInfo connectionInfo, String bridgeName) {
        return getBridge(connectionInfo, bridgeName, LogicalDatastoreType.OPERATIONAL);
    }

    /**
     * Extract the node contents from <code>store</code> type data store for the
     * bridge identified by <code>bridgeName</code>.
     *
     * @param connectionInfo address for the node
     * @param bridgeName name of the bridge
     * @param store defined by the <code>LogicalDatastoreType</code> enumeration
     * @return <code>store</code> type data store contents
     */
    public Node getBridgeNode(ConnectionInfo connectionInfo, String bridgeName, LogicalDatastoreType store) {
        InstanceIdentifier<Node> bridgeIid = createInstanceIdentifier(connectionInfo, new OvsdbBridgeName(bridgeName));
        return mdsalUtils.read(store, bridgeIid);
    }

    public Node getBridgeNode(Node node, String bridgeName) {
        OvsdbBridgeAugmentation bridge = extractBridgeAugmentation(node);
        if (bridge != null && bridge.getBridgeName().getValue().equals(bridgeName)) {
            return node;
        } else {
            return readBridgeNode(node, bridgeName);
        }
    }

    public Node readBridgeNode(Node node, String name) {
        Node ovsdbNode = node;
        if (extractNodeAugmentation(ovsdbNode) == null) {
            ovsdbNode = readOvsdbNode(node);
            if (ovsdbNode == null) {
                return null;
            }
        }
        Node bridgeNode = null;
        ConnectionInfo connectionInfo = getConnectionInfo(ovsdbNode);
        if (connectionInfo != null) {
            InstanceIdentifier<Node> bridgeIid =
                    createInstanceIdentifier(node.getKey(), name);
            bridgeNode = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, bridgeIid);
        }
        return bridgeNode;
    }

    public OvsdbNodeAugmentation extractNodeAugmentation(Node node) {
        return node.getAugmentation(OvsdbNodeAugmentation.class);
    }

    public OvsdbBridgeAugmentation extractBridgeAugmentation(Node node) {
        if (node == null) {
            return null;
        }
        return node.getAugmentation(OvsdbBridgeAugmentation.class);
    }

    public Node readOvsdbNode(Node bridgeNode) {
        Node ovsdbNode = null;
        OvsdbBridgeAugmentation bridgeAugmentation = extractBridgeAugmentation(bridgeNode);
        if (bridgeAugmentation != null) {
            InstanceIdentifier<Node> ovsdbNodeIid =
                    (InstanceIdentifier<Node>) bridgeAugmentation.getManagedBy().getValue();
            ovsdbNode = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, ovsdbNodeIid);
        }else{
            LOG.debug("readOvsdbNode: Provided node is not a bridge node : {}",bridgeNode);
        }
        return ovsdbNode;
    }

    public boolean deleteBridge(final ConnectionInfo connectionInfo, final String bridgeName) {
        return deleteBridge(connectionInfo, bridgeName, OVSDB_UPDATE_TIMEOUT);
    }

    public boolean deleteBridge(final ConnectionInfo connectionInfo, final String bridgeName, long timeout) {
        boolean result = mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION,
                createInstanceIdentifier(connectionInfo, new OvsdbBridgeName(bridgeName)));
        if (timeout != 0) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting after deleting bridge {}", bridgeName, e);
            }
        }
        return result;
    }

    public List<ProtocolEntry> createMdsalProtocols() {
        List<ProtocolEntry> protocolList = new ArrayList<>();
        ImmutableBiMap<String, Class<? extends OvsdbBridgeProtocolBase>> mapper =
                OVSDB_PROTOCOL_MAP.inverse();
        protocolList.add(new ProtocolEntryBuilder().setProtocol(mapper.get("OpenFlow13")).build());
        return protocolList;
    }

    public boolean addBridge(final ConnectionInfo connectionInfo, InstanceIdentifier<Node> bridgeIid,
                             final String bridgeName, NodeId bridgeNodeId, final boolean setProtocolEntries,
                             final Class<? extends OvsdbFailModeBase> failMode, final boolean setManagedBy,
                             final Class<? extends DatapathTypeBase> dpType,
                             final List<BridgeExternalIds> externalIds,
                             final List<ControllerEntry> controllerEntries,
                             final List<BridgeOtherConfigs> otherConfigs,
                             final String dpid) throws InterruptedException {
        return addBridge(connectionInfo, bridgeIid, bridgeName, bridgeNodeId, setProtocolEntries, failMode,
                setManagedBy, dpType, externalIds, controllerEntries, otherConfigs, dpid);
    }

    /*
     * base method for adding test bridges.  Other helper methods used to create bridges should utilize this method.
     *
     * @param connectionInfo
     * @param bridgeIid if passed null, one is created
     * @param bridgeName cannot be null
     * @param bridgeNodeId if passed null, one is created based on <code>bridgeIid</code>
     * @param setProtocolEntries toggles whether default protocol entries are set for the bridge
     * @param failMode toggles whether default fail mode is set for the bridge
     * @param setManagedBy toggles whether to setManagedBy for the bridge
     * @param dpType if passed null, this parameter is ignored
     * @param externalIds if passed null, this parameter is ignored
     * @param otherConfig if passed null, this parameter is ignored
     * @return success of bridge addition
     * @throws InterruptedException
     */
    public boolean addBridge(final ConnectionInfo connectionInfo, InstanceIdentifier<Node> bridgeIid,
                             final String bridgeName, NodeId bridgeNodeId, final boolean setProtocolEntries,
                             final Class<? extends OvsdbFailModeBase> failMode, final boolean setManagedBy,
                             final Class<? extends DatapathTypeBase> dpType,
                             final List<BridgeExternalIds> externalIds,
                             final List<ControllerEntry> controllerEntries,
                             final List<BridgeOtherConfigs> otherConfigs,
                             final String dpid, long timeout) throws InterruptedException {

        NodeBuilder bridgeNodeBuilder = new NodeBuilder();
        if (bridgeIid == null) {
            bridgeIid = createInstanceIdentifier(connectionInfo, new OvsdbBridgeName(bridgeName));
        }
        if (bridgeNodeId == null) {
            bridgeNodeId = createManagedNodeId(bridgeIid);
        }
        bridgeNodeBuilder.setNodeId(bridgeNodeId);
        OvsdbBridgeAugmentationBuilder ovsdbBridgeAugmentationBuilder = new OvsdbBridgeAugmentationBuilder();
        ovsdbBridgeAugmentationBuilder.setBridgeName(new OvsdbBridgeName(bridgeName));
        if (setProtocolEntries) {
            ovsdbBridgeAugmentationBuilder.setProtocolEntry(createMdsalProtocols());
        }
        if (failMode != null) {
            ovsdbBridgeAugmentationBuilder.setFailMode(failMode);
        }
        if (setManagedBy) {
            setManagedBy(ovsdbBridgeAugmentationBuilder, connectionInfo);
        }
        if (dpType != null) {
            ovsdbBridgeAugmentationBuilder.setDatapathType(dpType);
        }
        if (externalIds != null) {
            ovsdbBridgeAugmentationBuilder.setBridgeExternalIds(externalIds);
        }
        if (controllerEntries != null) {
            ovsdbBridgeAugmentationBuilder.setControllerEntry(controllerEntries);
        }
        if (otherConfigs != null) {
            ovsdbBridgeAugmentationBuilder.setBridgeOtherConfigs(otherConfigs);
        }
        if (dpid != null && !dpid.isEmpty()) {
            DatapathId datapathId = new DatapathId(dpid);
            ovsdbBridgeAugmentationBuilder.setDatapathId(datapathId);
        }
        bridgeNodeBuilder.addAugmentation(OvsdbBridgeAugmentation.class, ovsdbBridgeAugmentationBuilder.build());
        LOG.debug("Built with the intent to store bridge data {}",
                ovsdbBridgeAugmentationBuilder.toString());
        boolean result = mdsalUtils.merge(LogicalDatastoreType.CONFIGURATION,
                bridgeIid, bridgeNodeBuilder.build());
        if (timeout != 0) {
            Thread.sleep(OVSDB_UPDATE_TIMEOUT);
        }
        return result;
    }

    private void setManagedBy(final OvsdbBridgeAugmentationBuilder ovsdbBridgeAugmentationBuilder,
                              final ConnectionInfo connectionInfo) {
        InstanceIdentifier<Node> connectionNodePath = createInstanceIdentifier(connectionInfo);
        ovsdbBridgeAugmentationBuilder.setManagedBy(new OvsdbNodeRef(connectionNodePath));
    }

    public boolean addTerminationPoint(
            Node bridgeNode, String portName, String type, Map<String, String> options,
            Map<String, String> externalIds) {
        return addTerminationPoint(bridgeNode, portName, type, options, externalIds, null);
    }

    public boolean addTerminationPoint(
            Node bridgeNode, String portName, String type, Map<String, String> options, Map<String, String> externalIds,
            Long ofPort) {
        InstanceIdentifier<TerminationPoint> tpIid = createTerminationPointInstanceIdentifier(bridgeNode, portName);
        OvsdbTerminationPointAugmentationBuilder tpAugmentationBuilder = new OvsdbTerminationPointAugmentationBuilder();

        tpAugmentationBuilder.setName(portName);
        tpAugmentationBuilder.setOfport(ofPort);
        if (type != null) {
            tpAugmentationBuilder.setInterfaceType(OVSDB_INTERFACE_TYPE_MAP.get(type));
        }

        if (options != null && options.size() > 0) {
            List<Options> optionsList = new ArrayList<>();
            for (Map.Entry<String, String> entry : options.entrySet()) {
                OptionsBuilder optionsBuilder = new OptionsBuilder();
                optionsBuilder.setKey(new OptionsKey(entry.getKey()));
                optionsBuilder.setOption(entry.getKey());
                optionsBuilder.setValue(entry.getValue());
                optionsList.add(optionsBuilder.build());
            }
            tpAugmentationBuilder.setOptions(optionsList);
        }

        if (externalIds != null && externalIds.size() > 0) {
            List<InterfaceExternalIds> externalIdsList = new ArrayList<>();
            for (Map.Entry<String, String> entry : externalIds.entrySet()) {
                InterfaceExternalIdsBuilder interfaceExternalIdsBuilder = new InterfaceExternalIdsBuilder();
                interfaceExternalIdsBuilder.setKey(new InterfaceExternalIdsKey(entry.getKey()));
                interfaceExternalIdsBuilder.setExternalIdKey(entry.getKey());
                interfaceExternalIdsBuilder.setExternalIdValue(entry.getValue());
                externalIdsList.add(interfaceExternalIdsBuilder.build());
            }
            tpAugmentationBuilder.setInterfaceExternalIds(externalIdsList);
        }

        TerminationPointBuilder tpBuilder = new TerminationPointBuilder();
        tpBuilder.setKey(InstanceIdentifier.keyOf(tpIid));
        tpBuilder.addAugmentation(OvsdbTerminationPointAugmentation.class, tpAugmentationBuilder.build());
        /* TODO SB_MIGRATION should this be merge or mdsalUtils.put */
        return mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, tpIid, tpBuilder.build());
    }

    public Boolean addTerminationPoint(Node bridgeNode, String portName, String type) {
        return addTerminationPoint(bridgeNode, portName, type, null, null);
    }

    private String getControllerIPAddress() {
        String addressString = ConfigProperties.getProperty(this.getClass(), "ovsdb.controller.address");
        if (addressString != null) {
            try {
                if (InetAddress.getByName(addressString) != null) {
                    return addressString;
                }
            } catch (UnknownHostException e) {
                LOG.error("Host {} is invalid", addressString, e);
            }
        }

        addressString = ConfigProperties.getProperty(this.getClass(), "of.address");
        if (addressString != null) {
            try {
                if (InetAddress.getByName(addressString) != null) {
                    return addressString;
                }
            } catch (UnknownHostException e) {
                LOG.error("Host {} is invalid", addressString, e);
            }
        }

        return null;
    }

    private short getControllerOFPort() {
        short openFlowPort = OPENFLOW_PORT;
        String portString = ConfigProperties.getProperty(this.getClass(), "of.listenPort");
        if (portString != null) {
            try {
                openFlowPort = Short.parseShort(portString);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid port:{}, use default({})", portString,
                        openFlowPort, e);
            }
        }
        return openFlowPort;
    }

    public List<String> getControllersFromOvsdbNode(Node node) {
        List<String> controllersStr = new ArrayList<>();

        String controllerIpStr = getControllerIPAddress();
        if (controllerIpStr != null) {
            // If codepath makes it here, the ip address to be used was explicitly provided.
            // Being so, also fetch openflowPort provided via ConfigProperties.
            controllersStr.add(OPENFLOW_CONNECTION_PROTOCOL
                    + ":" + controllerIpStr + ":" + getControllerOFPort());
        } else {
            // Check if ovsdb node has manager entries
            OvsdbNodeAugmentation ovsdbNodeAugmentation = extractOvsdbNode(node);
            if (ovsdbNodeAugmentation != null) {
                List<ManagerEntry> managerEntries = ovsdbNodeAugmentation.getManagerEntry();
                if (managerEntries != null && !managerEntries.isEmpty()) {
                    for (ManagerEntry managerEntry : managerEntries) {
                        if (managerEntry == null || managerEntry.getTarget() == null) {
                            continue;
                        }
                        String[] tokens = managerEntry.getTarget().getValue().split(":");
                        if (tokens.length == 3 && tokens[0].equalsIgnoreCase("tcp")) {
                            controllersStr.add(OPENFLOW_CONNECTION_PROTOCOL
                                    + ":" + tokens[1] + ":" + getControllerOFPort());
                        } else if (tokens[0].equalsIgnoreCase("ptcp")) {
                            ConnectionInfo connectionInfo = ovsdbNodeAugmentation.getConnectionInfo();
                            if (connectionInfo != null && connectionInfo.getLocalIp() != null) {
                                controllerIpStr = String.valueOf(connectionInfo.getLocalIp().getValue());
                                controllersStr.add(OPENFLOW_CONNECTION_PROTOCOL
                                        + ":" + controllerIpStr + ":" + OPENFLOW_PORT);
                            } else {
                                LOG.warn("Ovsdb Node does not contain connection info: {}", node);
                            }
                        } else {
                            LOG.trace("Skipping manager entry {} for node {}",
                                    managerEntry.getTarget(), node.getNodeId().getValue());
                        }
                    }
                } else {
                    LOG.warn("Ovsdb Node does not contain manager entries : {}", node);
                }
            }
        }

        if (controllersStr.isEmpty()) {
            // Neither user provided ip nor ovsdb node has manager entries. Lets use local machine ip address.
            LOG.debug("Use local machine ip address as a OpenFlow Controller ip address");
            controllerIpStr = getLocalControllerHostIpAddress();
            if (controllerIpStr != null) {
                controllersStr.add(OPENFLOW_CONNECTION_PROTOCOL
                        + ":" + controllerIpStr + ":" + OPENFLOW_PORT);
            }
        }

        if (controllersStr.isEmpty()) {
            LOG.warn("Failed to determine OpenFlow controller ip address");
        } else if (LOG.isDebugEnabled()) {
            controllerIpStr = "";
            for (String currControllerIpStr : controllersStr) {
                controllerIpStr += " " + currControllerIpStr;
            }
            LOG.debug("Found {} OpenFlow Controller(s) :{}", controllersStr.size(), controllerIpStr);
        }

        return controllersStr;
    }

    private String getLocalControllerHostIpAddress() {
        String ipaddress = null;
        try{
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();){
                NetworkInterface iface = ifaces.nextElement();

                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress() && inetAddr.isSiteLocalAddress()) {
                        ipaddress = inetAddr.getHostAddress();
                        break;
                    }
                }
            }
        }catch (Exception e){
            LOG.warn("Exception while fetching local host ip address ", e);
        }
        return ipaddress;
    }

    public long getDataPathId(Node node) {
        long dpid = 0L;
        String datapathId = getDatapathId(node);
        if (datapathId != null) {
            dpid = new BigInteger(datapathId.replaceAll(":", ""), 16).longValue();
        }
        return dpid;
    }

    public String getDatapathId(Node node) {
        OvsdbBridgeAugmentation ovsdbBridgeAugmentation = node.getAugmentation(OvsdbBridgeAugmentation.class);
        return getDatapathId(ovsdbBridgeAugmentation);
    }

    public String getDatapathId(OvsdbBridgeAugmentation ovsdbBridgeAugmentation) {
        String datapathId = null;
        if (ovsdbBridgeAugmentation != null && ovsdbBridgeAugmentation.getDatapathId() != null) {
            datapathId = ovsdbBridgeAugmentation.getDatapathId().getValue();
        }
        return datapathId;
    }
}
