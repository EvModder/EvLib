package net.evmodder.EvLib.util;

import java.util.UUID;

public record PearlData(UUID owner, int submitterId, long submittedTs, long lastAccessed){}