package main

import (
    "fmt"
    "log"
    "os"
    "sync"
    "time"
)

type Task struct {
    ID int
}

func (t Task) process() {
    time.Sleep(500 * time.Millisecond)
    fmt.Printf("Processed Task: %d\n", t.ID)
}

func worker(id int, tasks <-chan Task, results *[]string, wg *sync.WaitGroup, mu *sync.Mutex) {
    defer wg.Done()
    for task := range tasks {
        task.process()
        mu.Lock()
        *results = append(*results, fmt.Sprintf("Result of Task %d", task.ID))
        mu.Unlock()
    }
}

func main() {
    tasks := make(chan Task, 10)
    var results []string
    var wg sync.WaitGroup
    var mu sync.Mutex

    for i := 0; i < 10; i++ {
        tasks <- Task{ID: i}
    }
    close(tasks)

    for i := 0; i < 4; i++ {
        wg.Add(1)
        go worker(i, tasks, &results, &wg, &mu)
    }

    wg.Wait()

    file, err := os.Create("output.txt")
    if err != nil {
        log.Fatal(err)
    }
    defer file.Close()
    for _, res := range results {
        fmt.Fprintln(file, res)
    }
}