package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.BlockingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Learn how to call blocking code from Reactive one with adapted concurrency strategy for blocking code that produces
 * or receives data.
 *
 * For those who know RxJava: - RxJava subscribeOn = Reactor subscribeOn - RxJava observeOn = Reactor publishOn - RxJava
 * Schedulers.io <==> Reactor Schedulers.elastic
 *
 * @author Sebastien Deleuze
 * @see Flux#subscribeOn(Scheduler)
 * @see Flux#publishOn(Scheduler)
 * @see Schedulers
 */
public class Part11BlockingToReactive {

    // subscribeOn 和 publishOn 的影响范围和顺序是有规律的，查看文档。

    // ========================================================================================

    // TODO Create a Flux for reading all users from the blocking repository deferred until the flux is subscribed, and
    // run it with an elastic scheduler
    Flux<User> blockingRepositoryToFlux(BlockingRepository<User> repository) {
        // 先把阻塞的动作包装成 Flux，使用 defer 防止立马执行阻塞代码块（直接用 fromIterable 会立即执行阻塞代码）。
        // 然后在订阅到一个线程调度器上，一旦 Flux subscribe 就触发阻塞代码块的执行。
        // 错误的写法：Flux.fromIterable(repository.findAll()).subscribeOn(Schedulers.elastic());
        return Flux.defer(() -> Flux.fromIterable(repository.findAll())).subscribeOn(Schedulers.elastic());
    }

    // ========================================================================================

    // TODO Insert users contained in the Flux parameter in the blocking repository using an elastic scheduler and
    // return a Mono<Void> that signal the end of the operation
    Mono<Void> fluxToBlockingRepository(Flux<User> flux, BlockingRepository<User> repository) {
        // 先把 Flux 发布到一个线程调度器上，然后在慢慢消费
        return flux.publishOn(Schedulers.elastic()).doOnNext(repository::save).then();
    }

}
