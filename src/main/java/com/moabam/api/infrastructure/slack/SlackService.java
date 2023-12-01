package com.moabam.api.infrastructure.slack;

import java.io.IOException;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlackService {

	private final SlackApi slackApi;
	private final SlackMessageFactory slackMessageFactory;
	private final TaskExecutor taskExecutor;

	public void send(HttpServletRequest request, Exception exception) throws IOException {
		SlackMessage slackMessage = slackMessageFactory.generateErrorMessage(request, exception);
		taskExecutor.execute(() -> slackApi.call(slackMessage));
	}
}
