/*
 * Warning: This is a generated file. Edit at your own risk.
 * generated by Gen.hs on Wed Oct 28 18:26:49 CET 2009.
 */

package de.htwk.autolat.Connector.types;
import java.util.List;

@SuppressWarnings("unused")
public class ServerInfo
{
    private final Version protocolVersion;
    private final String serverName;
    private final Version serverVersion;
    
    public ServerInfo(Version protocolVersion,
                      String serverName,
                      Version serverVersion)
    {
        this.protocolVersion = protocolVersion;
        this.serverName = serverName;
        this.serverVersion = serverVersion;
    }
    
    public Version getProtocolVersion()
    {
        return protocolVersion;
    }
    
    public String getServerName()
    {
        return serverName;
    }
    
    public Version getServerVersion()
    {
        return serverVersion;
    }
    
    public String toString()
    {
        return "ServerInfo("
            + protocolVersion + ", "
            + serverName + ", "
            + serverVersion + ")";
    }
    
    public boolean equals(Object other)
    {
        if (! (other instanceof ServerInfo))
            return false;
        ServerInfo oServerInfo = (ServerInfo) other;
        if (!protocolVersion.equals(oServerInfo.getProtocolVersion()))
            return false;
        if (!serverName.equals(oServerInfo.getServerName()))
            return false;
        if (!serverVersion.equals(oServerInfo.getServerVersion()))
            return false;
        return true;
    }
    
    public int hashCode()
    {
        return
            protocolVersion.hashCode() * 1 +
            serverName.hashCode() * 37 +
            serverVersion.hashCode() * 1369;
    }
    
}