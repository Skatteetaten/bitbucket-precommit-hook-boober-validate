package no.skatteetaten.aurora.bitbucket.hook;

import com.atlassian.bitbucket.commit.*;
import com.atlassian.bitbucket.content.*;
import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

@Scanned
public class BooberVerifyer implements PreRepositoryHook<RepositoryHookRequest> {


	private ContentService contentService;
	private CommitService commitService;

	public BooberVerifyer(@ComponentImport final ContentService contentService, @ComponentImport final CommitService commitService) {

		this.contentService = contentService;
		this.commitService = commitService;
	}

	@Nonnull
	@Override
	public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context, @Nonnull RepositoryHookRequest request) {

		final Repository repo = request.getRepository();
		final Map<String, ByteArrayOutputStream> filesStreams = new HashMap();

		request.getRefChanges().stream()
				.filter(change -> change.getRef().getId().equalsIgnoreCase("refs/heads/master"))
				.forEach(refChange -> {

					final ChangesRequest changesreq = new ChangesRequest.Builder(repo, refChange.getToHash()).sinceId(refChange.getFromHash()).build();
					commitService.streamChanges(changesreq, change -> {

						String fileName = change.getPath().toString();
						contentService.streamFile(repo, changesreq.getUntilId(), fileName, arg0 -> {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							filesStreams.put(fileName, baos);
							return baos;
						});
						return true;
					});
				});

		return RepositoryHookResult.accepted();
	}

}
