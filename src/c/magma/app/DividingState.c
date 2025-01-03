package magma.app;package magma.app;

import java.util.ArrayList;package magma.app;

import java.util.ArrayList;
import java.util.List;package magma.app;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer) {
    DividingState append(char c) {
        buffer().append(c);package magma.app;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer) {
    DividingState append(char c) {
        buffer().append(c);
        return this;package magma.app;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer) {
    DividingState append(char c) {
        buffer().append(c);
        return this;
    }

    DividingState advance() {
        if (buffer.isEmpty()) {
            return this;package magma.app;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer) {
    DividingState append(char c) {
        buffer().append(c);
        return this;
    }

    DividingState advance() {
        if (buffer.isEmpty()) {
            return this;
        }
        final var copy = new ArrayList<>(segments);package magma.app;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer) {
    DividingState append(char c) {
        buffer().append(c);
        return this;
    }

    DividingState advance() {
        if (buffer.isEmpty()) {
            return this;
        }
        final var copy = new ArrayList<>(segments);
        copy.add(buffer().toString());package magma.app;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer) {
    DividingState append(char c) {
        buffer().append(c);
        return this;
    }

    DividingState advance() {
        if (buffer.isEmpty()) {
            return this;
        }
        final var copy = new ArrayList<>(segments);
        copy.add(buffer().toString());
        return new DividingState(copy, buffer);package magma.app;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer) {
    DividingState append(char c) {
        buffer().append(c);
        return this;
    }

    DividingState advance() {
        if (buffer.isEmpty()) {
            return this;
        }
        final var copy = new ArrayList<>(segments);
        copy.add(buffer().toString());
        return new DividingState(copy, buffer);
    }
}