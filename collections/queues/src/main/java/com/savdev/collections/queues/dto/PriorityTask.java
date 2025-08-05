package com.savdev.collections.queues.dto;

import java.util.Comparator;

public record PriorityTask(Task task, Priority priority)
  implements Comparable<PriorityTask> {

  static Comparator<PriorityTask> priorityTaskCmpr =
    Comparator.comparing(PriorityTask::priority)
    .thenComparing(PriorityTask::task);

  public int compareTo(PriorityTask pt) {
    return priorityTaskCmpr.compare(this, pt);
  }
}
