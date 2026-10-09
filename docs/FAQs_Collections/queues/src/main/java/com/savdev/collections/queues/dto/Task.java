package com.savdev.collections.queues.dto;

public interface Task extends Comparable<Task> {
  default int compareTo(Task t) {
    return toString().compareTo(t.toString());
  }
}