package no.skatteetaten.aurora.bitbucket.hook;

import com.atlassian.bitbucket.content.*;
import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Scanned
public class BooberVerifyer implements PreReceiveRepositoryHook {


	private ContentService contentService;

	public BooberVerifyer(@ComponentImport final ContentService contentService) {

		this.contentService = contentService;
	}

	/**
	 * Disables deletion of branches
	 */
	public boolean onReceive(RepositoryHookContext context, Collection<RefChange> refChanges, HookResponse hookResponse) {
		hookResponse.out().println("=================================");


		final List<String> files = new ArrayList();
		ContentTreeCallback callback = new ContentTreeCallback() {
			public void onEnd(@Nonnull ContentTreeSummary contentTreeSummary) throws IOException {

			}

			public void onStart(@Nonnull ContentTreeContext contentTreeContext) throws IOException {

			}

			public boolean onTreeNode(@Nonnull ContentTreeNode node) throws IOException {

				files.add(node.getPath().toString());


				return true;
			}
		};

		PageRequest pr = new PageRequestImpl(0, 10000);
		contentService.streamDirectory(context.getRepository(), "master", "/", true, callback, pr);


		for (RefChange refChange : refChanges) {
			if (refChange.getType() == RefChangeType.DELETE) {
				hookResponse.err().println("The ref '" + refChange.getRefId() + "' cannot be deleted.");
				return false;
			}
		}
		return true;
	}
}
