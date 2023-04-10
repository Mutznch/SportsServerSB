package br.pucpr.sportsserver.rest.discussions;

import br.pucpr.sportsserver.lib.exception.BadRequestException;
import br.pucpr.sportsserver.lib.exception.ForbiddenException;
import br.pucpr.sportsserver.lib.exception.NotFoundException;
import br.pucpr.sportsserver.rest.discussions.replies.Reply;
import br.pucpr.sportsserver.rest.discussions.replies.RepliesRepository;
import br.pucpr.sportsserver.rest.discussions.request.DiscussionRequest;
import br.pucpr.sportsserver.rest.discussions.request.ReplyRequest;
import br.pucpr.sportsserver.rest.discussions.response.DiscussionResponse;
import br.pucpr.sportsserver.rest.discussions.response.ReplyResponse;
import br.pucpr.sportsserver.rest.sports.SportsRepository;
import br.pucpr.sportsserver.rest.users.User;
import br.pucpr.sportsserver.rest.users.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionsService {
    private DiscussionsRepository discussionsRepository;
    private RepliesRepository repliesRepository;
    private UsersRepository usersRepository;
    private SportsRepository sportsRepository;

    public DiscussionsService(
            DiscussionsRepository discussionsRepository,
            RepliesRepository repliesRepository,
            UsersRepository usersRepository,
            SportsRepository sportsRepository
    ) {
        this.discussionsRepository = discussionsRepository;
        this.repliesRepository = repliesRepository;
        this.usersRepository = usersRepository;
        this.sportsRepository = sportsRepository;
    }

    private Discussion reqToDiscussion(DiscussionRequest reqDiscussion, User user) {
        return new Discussion(
                user,
                sportsRepository.findById(reqDiscussion.getSport())
                        .orElseThrow(() -> new BadRequestException("Invalid Sport Name")),
                reqDiscussion.getTitle(),
                reqDiscussion.getText()
        );
    }

    private Reply reqToReply(ReplyRequest reqReply, User user) {
        return new Reply(
                discussionsRepository.findById(reqReply.getDiscussionId())
                        .orElseThrow(() -> new NotFoundException("Discussion id not found")),
                user,
                reqReply.getText()
        );
    }

    private DiscussionResponse discussionToRes(Discussion discussion) {
        return new DiscussionResponse(
                discussion.getId(),
                discussion.getUser().getUsername(),
                discussion.getSport().getName(),
                discussion.getTitle(),
                discussion.getText(),
                discussion.getReplies().stream().map(this::replyToRes).collect(Collectors.toSet())
        );
    }

    private ReplyResponse replyToRes(Reply reply) {
        return new ReplyResponse(
                reply.getId(),
                reply.getDiscussion().getId(),
                reply.getUser().getUsername(),
                reply.getText()
        );
    }

    public List<DiscussionResponse> searchDiscussions(Long userId, String username, String title, String sport) {
        if (userId != null)
            return usersRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Id \"" + userId + "\" Not Found"))
                    .getDiscussions().stream().map(this::discussionToRes).toList();
        if (username != null && sport != null)
            return usersRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"))
                    .getDiscussions().stream().filter(d -> d.getSport().getName() == sport)
                    .map(this::discussionToRes).toList();
        if (username != null)
            return usersRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"))
                    .getDiscussions().stream().map(this::discussionToRes).toList();

        if (title != null && sport != null)
            return discussionsRepository.findAllByTitleAndSportName(title, sport)
                    .stream().map(this::discussionToRes).toList();
        if (title != null)
            return discussionsRepository.findAllByTitle(title)
                    .stream().map(this::discussionToRes).toList();
        if (sport != null)
            return sportsRepository.findById(sport)
                    .orElseThrow(() -> new BadRequestException("Invalid Sport Name"))
                    .getDiscussions().stream().map(this::discussionToRes).toList();
        return discussionsRepository.findAll()
                .stream().map(this::discussionToRes).toList();
    }

    public List<ReplyResponse> searchReplies(Long id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"))
                .getReplies().stream().map(this::replyToRes).toList();
    }

    public List<ReplyResponse> searchRepliesByUsername(String username) {
        return searchReplies(usersRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Username \"" + username + "\" Not Found"))
                .getId()
        );
    }

    public DiscussionResponse addDiscussion(Long id, DiscussionRequest reqDiscussion) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        var discussion = reqToDiscussion(reqDiscussion, user);
        user.getDiscussions().add(discussion);
        return discussionToRes(discussionsRepository.save(discussion));
    }

    public ReplyResponse addReply(Long id, Long discussionId, ReplyRequest reqReply) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        var reply = reqToReply(reqReply, user);
        user.getReplies().add(reply);
        return replyToRes(repliesRepository.save(reply));
    }

    public void deleteDiscussion(Long id, Long discussionId) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        var discussion = discussionsRepository.findById(discussionId)
                .orElseThrow(() -> new NotFoundException("Discussion Id Not Found"));
        if (discussion.getUser().getId() != id && !user.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only discussion creator can access this route");
        discussion.getReplies().forEach(r -> {
            r.clearReply();
            repliesRepository.deleteById(r.getId());
        });
        discussion.clearDiscussion();
        discussionsRepository.deleteById(discussion.getId());
    }

    public void deleteReply(Long id, Long replyId) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        var reply = repliesRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Reply Id Not Found"));
        if (reply.getUser().getId() != id && !user.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only reply creator can access this route");
        reply.clearReply();
        repliesRepository.deleteById(reply.getId());
    }

    public DiscussionResponse updateDiscussion(Long id, Long discussionId, String title, String text, String sport) {
        var discussion = discussionsRepository.findById(discussionId)
                .orElseThrow(() -> new NotFoundException("Discussion Id Not Found"));
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (discussion.getUser().getId() != id && !user.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only discussion creator can access this route");
        if (title != null) discussion.setTitle(title);
        if (text != null) discussion.setText(text);
        if (sport != null) discussion
                .setSport(sportsRepository.findById(sport)
                .orElseThrow(() -> new BadRequestException("Invalid Sport Name")));
        return discussionToRes(discussionsRepository.save(discussion));
    }

    public ReplyResponse updateReply(Long id, Long replyId, String text) {
        var reply = repliesRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("Reply Id Not Found"));
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Id \"" + id + "\" Not Found"));
        if (reply.getUser().getId() != id && !user.getRoles().contains("ADMIN"))
            throw new ForbiddenException("Only discussion creator can access this route");
        if (text == null)
            throw new BadRequestException("Text can't be null");
        reply.setText(text);
        return replyToRes(repliesRepository.save(reply));
    }
}
