package pro.gravit.launcher.client.gui;

import pro.gravit.launcher.events.request.AuthRequestEvent;
import pro.gravit.launcher.events.request.PingServerRequestEvent;
import pro.gravit.launcher.events.request.ProfilesRequestEvent;
import pro.gravit.launcher.profiles.ClientProfile;
import pro.gravit.launcher.profiles.PlayerProfile;
import pro.gravit.launcher.profiles.optional.OptionalFile;
import pro.gravit.launcher.profiles.optional.OptionalTrigger;
import pro.gravit.launcher.request.Request;
import pro.gravit.launcher.request.management.PingServerReportRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeStateMachine {
    private AuthRequestEvent rawAuthResult;

    private List<ClientProfile> profiles;
    private ClientProfile profile;
    private Map<String, PingServerReportRequest.PingServerReport> serverPingReport;
    @FunctionalInterface
    public interface OnServerPingReportCallback
    {
        void onServerPingReport(PingServerReportRequest.PingServerReport report);
    }
    private final Map<String, OnServerPingReportCallback> serverPingReportCallbackMap = new HashMap<>();

    public void setAuthResult(AuthRequestEvent rawAuthResult) {
        this.rawAuthResult = rawAuthResult;
        if (rawAuthResult.session != 0)
            Request.setSession(rawAuthResult.session);
    }

    public Map<String, PingServerReportRequest.PingServerReport> getServerPingReport() {
        return serverPingReport;
    }

    public void setServerPingReport(Map<String, PingServerReportRequest.PingServerReport> serverPingReport) {
        this.serverPingReport = serverPingReport;
        serverPingReportCallbackMap.forEach((name, callback) -> {
            PingServerReportRequest.PingServerReport report = serverPingReport.get(name);
            callback.onServerPingReport(report);
        });
    }

    public void addServerPingCallback(String name, OnServerPingReportCallback callback)
    {
        if(serverPingReport != null)
        {
            PingServerReportRequest.PingServerReport report = serverPingReport.get(name);
            callback.onServerPingReport(report);
        }
        serverPingReportCallbackMap.put(name, callback);
    }

    public void clearServerPingCallbacks()
    {
        serverPingReportCallbackMap.clear();
    }

    public void setProfilesResult(ProfilesRequestEvent rawProfilesResult) {
        this.profiles = rawProfilesResult.profiles;
        this.profiles.sort(ClientProfile::compareTo);
        for (ClientProfile profile : this.profiles) {
            for (OptionalFile optionalFile : profile.getOptional()) {
                if (optionalFile.triggers == null)
                    continue;

                boolean anyTriggered = false;
                boolean anyNeed = false;
                boolean allNeedTriggered = false;

                for (OptionalTrigger trigger : optionalFile.triggers) {
                    boolean isTriggered = trigger.isTriggered();
                    if (isTriggered)
                        anyTriggered = true;
                    if (trigger.need) {
                        if (!anyNeed) {
                            anyNeed = true;
                            allNeedTriggered = isTriggered;
                        } else {
                            if (allNeedTriggered)
                                allNeedTriggered = isTriggered;
                        }
                    }
                }

                if (!anyNeed) {
                    if (anyTriggered)
                        profile.markOptional(optionalFile);
                } else {
                    if (allNeedTriggered) {
                        profile.markOptional(optionalFile);
                    } else {
                        optionalFile.visible = false;
                        profile.unmarkOptional(optionalFile);
                    }
                }
            }
        }
    }

    public String getUsername() {
        if (rawAuthResult == null || rawAuthResult.playerProfile == null)
            return "Player";
        return rawAuthResult.playerProfile.username;
    }

    public List<ClientProfile> getProfiles() {
        return profiles;
    }

    public ClientProfile getProfile() {
        return profile;
    }

    public void setProfile(ClientProfile profile) {
        this.profile = profile;
    }

    public PlayerProfile getPlayerProfile() {
        if (rawAuthResult == null)
            return null;
        return rawAuthResult.playerProfile;
    }

    public String getAccessToken() {
        if (rawAuthResult == null)
            return null;
        return rawAuthResult.accessToken;
    }

    public void exit() {
        rawAuthResult = null;
        profile = null;
    }
}
