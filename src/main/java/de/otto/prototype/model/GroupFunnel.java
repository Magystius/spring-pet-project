package de.otto.prototype.model;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Charsets.UTF_8;

public enum GroupFunnel implements Funnel<Group> {
	INSTANCE;

	@ParametersAreNonnullByDefault
	public void funnel(final Group group, final PrimitiveSink into) {
		into
				.putString(group.getId(), UTF_8)
				.putString(group.getName(), UTF_8)
				.putBoolean(group.isVip());
		group.getUserIds().forEach(userId -> into.putString(userId, UTF_8));
	}
}